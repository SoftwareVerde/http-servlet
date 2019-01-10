package com.softwareverde.httpserver;

import com.softwareverde.httpserver.endpoint.EncryptionRedirectEndpoint;
import com.softwareverde.httpserver.endpoint.NotFoundJsonEndpoint;
import com.softwareverde.security.tls.TlsCertificate;
import com.softwareverde.security.tls.TlsFactory;
import com.softwareverde.servlet.Endpoint;
import com.softwareverde.servlet.Servlet;
import com.softwareverde.servlet.WebSocketEndpoint;
import com.softwareverde.servlet.WebSocketServlet;
import com.softwareverde.servlet.request.Request;
import com.softwareverde.servlet.response.Response;
import com.softwareverde.util.IoUtil;
import com.softwareverde.util.StringUtil;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    public interface RequestHandler {
        Response onRequest(Request request);
        Boolean isStrictPathEnabled();
    }

    private Map<String, com.sun.net.httpserver.HttpHandler> _endpoints = new HashMap<String, com.sun.net.httpserver.HttpHandler>();

    private com.sun.net.httpserver.HttpServer _server;
    private Integer _port = 80;
    private Boolean _disableHttp = false;

    private com.sun.net.httpserver.HttpServer _tlsServer;
    private Integer _tlsPort = 443;
    private Boolean _useEncryption = false;
    private Boolean _redirectToTls = false;
    private String _certificateFile = null;
    private String _certificateKeyFile = null;
    private final Integer _maxConnectionCount;
    private final Integer _maxWebSocketPacketByteCount;

    private Servlet _defaultEndpoint = new NotFoundJsonEndpoint();
    private EncryptionRedirectEndpoint _encryptionRedirectEndpoint = new EncryptionRedirectEndpoint();

    private void _applyEndpoints(final com.sun.net.httpserver.HttpServer httpServer) {
        for (final String endpointUri : _endpoints.keySet()) {
            final com.sun.net.httpserver.HttpHandler handler = _endpoints.get(endpointUri);
            httpServer.createContext(endpointUri, handler);
        }

        if (! _endpoints.containsKey("/")) {
            httpServer.createContext("/", new HttpHandler(_defaultEndpoint, false));
        }
    }

    public HttpServer() {
        _encryptionRedirectEndpoint.setTlsPort(_tlsPort);
        _maxConnectionCount = 256;
        _maxWebSocketPacketByteCount = 8192;
    }

    /**
     * @param maxConnectionCount The max number of concurrent HTTP/HTTPS connections to the server.
     * @param webSocketPacketMaxByteCount The max number of bytes for a single inbound WebSocket Frame.
     */
    public HttpServer(final Integer maxConnectionCount, final Integer webSocketPacketMaxByteCount) {
        _encryptionRedirectEndpoint.setTlsPort(_tlsPort);
        _maxConnectionCount = maxConnectionCount;
        _maxWebSocketPacketByteCount = webSocketPacketMaxByteCount;
    }

    /**
     * Define an Endpoint to fulfill any requests matched by the endpoint path.
     *  NOTE: Setting this value after HttpServer.start() has been invoked will have no effect.
     */
    public void addEndpoint(final Endpoint endpoint) {
        final String path = endpoint.getPath();
        final Servlet servlet = endpoint.getServlet();
        final Boolean shouldUseStrictPath = endpoint.shouldUseStrictPath();

        _endpoints.put(path, new HttpHandler(servlet, shouldUseStrictPath));
    }

    /**
     * Define an Endpoint to handle new WebSocket requests to the provided endpoint path.
     *  NOTE: Setting this value after HttpServer.start() has been invoked will have no effect.
     */
    public void addEndpoint(final WebSocketEndpoint endpoint) {
        final String path = endpoint.getPath();
        final WebSocketServlet servlet = endpoint.getServlet();
        final Boolean shouldUseStrictPath = endpoint.shouldUseStrictPath();

        _endpoints.put(path, new WebSocketHandler(servlet, shouldUseStrictPath, _maxWebSocketPacketByteCount));
    }

    /**
     * Disables all non-encrypted (HTTP) requests.
     *  Disabling HTTP will also disable any redirection set by HttpServer.redirectToTls().
     *  Setting this value after HttpServer.start() has been invoked will have no effect.
     */
    public void disableHttp(final Boolean disableHttp) { _disableHttp = disableHttp; }

    /**
     * Sets the HTTP port for the server to listen on.
     *  The default value is 80.
     *  Setting this value after HttpServer.start() has been invoked will have no effect.
     */
    public void setPort(final Integer port) { _port = port; }

    /**
     * Sets the HTTPS port for the server to listen on.
     *  The default value is 443.
     *  Setting this value after HttpServer.start() has been invoked will have no effect.
     */
    public void setTlsPort(final Integer tlsPort) {
        _tlsPort = tlsPort;
        _encryptionRedirectEndpoint.setTlsPort(_tlsPort);
    }

    /**
     * Enables HTTPS for this server.
     *  A TLS certificate and key must be set before HttpServer.start() is invoked.
     *  To enable HTTPS, this value, and a key/certificate, must be set before HttpServer.start() is invoked.
     *  TLSv1.1 and TLSv1.2 are the only enabled protocols.
     */
    public void enableEncryption(final Boolean useEncryption) { _useEncryption = useEncryption; }

    /**
     * Sets the TLS certificate and key used to encryption.
     *  To enable encryption, this function and HttpServer.enableEncryption() must be invoked before HttpServer.start() is invoked.
     * @param tlsCertificateFile    - The path to the certificate file.
     * @param tlsCertificateKeyFile - The path to the unencrypted key file. This key must be in PKCS12, which is a binary file.
     *                                Use ./ssl/pem2pkcs.sh to convert an RSA key to PKCS12.
     */
    public void setCertificate(final String tlsCertificateFile, final String tlsCertificateKeyFile) {
        _certificateFile = tlsCertificateFile;
        _certificateKeyFile = tlsCertificateKeyFile;
    }

    /**
     * Enabling HttpServer.redirectToTls() will redirect all HTTP requests to HTTPS via a 301 Header.
     *  Encryption and HTTP must be enabled to allow this feature.
     */
    public void redirectToTls(final Boolean forceEncryption) { _redirectToTls = forceEncryption; }

    public Boolean start() {
        try {
            final ExecutorService executor = Executors.newFixedThreadPool(_maxConnectionCount);

            if (_useEncryption) {
                final HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(_tlsPort), _maxConnectionCount);
                final TlsCertificate tlsCertificate = TlsFactory.loadTlsCertificate(StringUtil.bytesToString(IoUtil.getFileContents(_certificateFile)), IoUtil.getFileContents(_certificateKeyFile));
                final SSLContext sslContext = TlsFactory.createContext(tlsCertificate);

                httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    @Override
                    public void configure(final HttpsParameters params) {
                        params.setProtocols(new String[]{ "TLSv1.1", "TLSv1.2", "TLSv1.3" });
                        params.setNeedClientAuth(false);
                    }
                });

                _applyEndpoints(httpsServer);
                httpsServer.setExecutor(executor);

                _tlsServer = httpsServer;
                _tlsServer.start();
            }

            if (! _disableHttp) {
                _server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(_port), _maxConnectionCount);

                if (_redirectToTls) {
                    _server.createContext("/", new HttpHandler(_encryptionRedirectEndpoint, false));
                }
                else {
                    _applyEndpoints(_server);
                }

                _server.setExecutor(executor);
                _server.start();
            }

            return true;
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public void stop() {
        _server.stop(0);
        if (_tlsServer != null) {
            _tlsServer.stop(0);
        }

        final ExecutorService executorService = ((ExecutorService) _server.getExecutor());
        executorService.shutdown();
    }
}
