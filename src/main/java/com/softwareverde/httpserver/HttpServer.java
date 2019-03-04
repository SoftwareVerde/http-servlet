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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    public interface RequestHandler {
        Response onRequest(Request request);
        Boolean isStrictPathEnabled();
    }

    protected Map<String, com.sun.net.httpserver.HttpHandler> _endpoints = new HashMap<String, com.sun.net.httpserver.HttpHandler>();

    protected com.sun.net.httpserver.HttpServer _server;
    protected Integer _port = 80;
    protected Boolean _disableHttp = false;

    protected com.sun.net.httpserver.HttpServer _tlsServer;
    protected Integer _tlsPort = 443;
    protected Boolean _useEncryption = false;
    protected Boolean _redirectToTls = false;
    protected final List<String> _certificateFiles = new ArrayList<String>();
    protected final List<String> _certificateKeyFiles = new ArrayList<String>();
    protected final Integer _maxConnectionCount;
    protected final Integer _maxWebSocketPacketByteCount;

    protected Servlet _defaultEndpoint = new NotFoundJsonEndpoint();
    protected EncryptionRedirectEndpoint _encryptionRedirectEndpoint = new EncryptionRedirectEndpoint();

    protected void _applyEndpoints(final com.sun.net.httpserver.HttpServer httpServer) {
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
        _certificateFiles.clear();
        _certificateKeyFiles.clear();

        _certificateFiles.add(tlsCertificateFile);
        _certificateKeyFiles.add(tlsCertificateKeyFile);
    }

    public void addCertificate(final String tlsCertificateFile, final String tlsCertificateKeyFile) {
        if ( (tlsCertificateFile == null) || (tlsCertificateKeyFile == null) ) { throw new NullPointerException("Secondary TLS Certificates cannot be null."); }

        _certificateFiles.add(tlsCertificateFile);
        _certificateKeyFiles.add(tlsCertificateKeyFile);
    }

    /**
     * Enabling HttpServer.redirectToTls() will redirect all HTTP requests to HTTPS via a 301 Header.
     *  Encryption and HTTP must be enabled to allow this feature.
     */
    public void redirectToTls(final Boolean forceEncryption) { _redirectToTls = forceEncryption; }

    public void redirectToTls(final Boolean forceEncryption, final Integer externalTlsPort) {
        _redirectToTls = forceEncryption;
        _encryptionRedirectEndpoint.setTlsPort(externalTlsPort);
    }

    protected void _loadCertificate(final TlsFactory tlsFactory, final String certificateFile, final String certificateKeyFile) {
        if (certificateFile == null || certificateKeyFile == null) { return; }

        final byte[] certificateBytes = IoUtil.getFileContents(certificateFile);
        final byte[] certificateKeyFileBytes = IoUtil.getFileContents(certificateKeyFile);
        if ( (certificateBytes == null) || (certificateKeyFileBytes == null) ) {
            System.out.println("Error loading certificate: " + certificateFile + ", " + certificateKeyFile);
            return;
        }

        tlsFactory.addTlsCertificate(StringUtil.bytesToString(certificateBytes), certificateKeyFileBytes);
    }

    public Boolean start() {
        try {
            final ExecutorService executor = Executors.newFixedThreadPool(_maxConnectionCount);

            if (_useEncryption) {
                final TlsFactory tlsFactory = new TlsFactory();

                final HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(_tlsPort), _maxConnectionCount);
                {
                    for (int i = 0 ; i < _certificateKeyFiles.size(); ++i) {
                        final String certificateFile = _certificateFiles.get(i);
                        final String certificateKeyFile = _certificateKeyFiles.get(i);

                        _loadCertificate(tlsFactory, certificateFile, certificateKeyFile);
                    }
                }

                final TlsCertificate tlsCertificate = tlsFactory.buildCertificate();
                final SSLContext sslContext = tlsCertificate.createContext();

                httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    @Override
                    public void configure(final HttpsParameters httpsParameters) {
                        httpsParameters.setProtocols(new String[]{ "TLSv1.1", "TLSv1.2", "TLSv1.3" });
                        httpsParameters.setNeedClientAuth(false);
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
