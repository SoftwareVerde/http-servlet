package com.softwareverde.httpserver;

import com.softwareverde.httpserver.response.JsonResult;
import com.softwareverde.util.Util;
import com.softwareverde.util.security.TlsCertificate;
import com.softwareverde.util.security.TlsFactory;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @param <T>   - The type of the AuthorizationObject managed by authorization keys.
 */
public class ApiServer<T> {
    public static class Response {
        public static class ResponseCodes {
            public static final Integer OK = 200;
            public static final Integer MOVED_PERMANENTLY = 301;
            public static final Integer MOVED = 302;
            public static final Integer BAD_REQUEST = 400;
            public static final Integer NOT_AUTHORIZED = 401;
            public static final Integer NOT_FOUND = 404;
            public static final Integer SERVER_ERROR = 500;
        }
        public static class Headers {
            public static final String CONTENT_TYPE = "Content-Type";
            public static final String CONTENT_ENCODING = "Content-Encoding";
        }

        private Map<String, List<String>> _headers = new HashMap<String, List<String>>();

        private Integer _code = ResponseCodes.OK;
        private byte[] _content = (new byte[0]);

        public Response() { }

        public void setContent(final byte[] content) {
            _content = content;
        }

        public void setContent(final String content) {
            try {
                _content = content.getBytes("UTF-8");
            } catch (final Exception e) { }
        }

        public void setCode(final Integer code) { _code = code; }

        public Integer getCode() { return _code; }
        public byte[] getContent() { return _content; }

        public void clearHeaders() {
            _headers.clear();
        }

        public void clearHeader(final String key) {
            if (! _headers.containsKey(key)) { return; }

            _headers.get(key).clear();
        }

        public void addHeader(final String key, final String value) {
            if (! _headers.containsKey(key)) {
                _headers.put(key, new ArrayList<String>());
            }

            _headers.get(key).add(value);
        }

        public Map<String, List<String>> getHeaders() { return _headers; }
    }

    public interface RequestHandler<T> {
        ApiServer<T> getServer();
        Boolean isStrictPathEnabled();
    }

    public static abstract class Endpoint<T> implements RequestHandler<T> {
        public abstract Response onRequest(GetParameters getParameters, PostParameters postParameters);

        private ApiServer<T> _server;
        public Endpoint(final ApiServer<T> apiServer) { _server = apiServer; }

        private Boolean _strictPathEnabled = false;
        public void setStrictPathEnabled(final Boolean strictPathEnabled) {
            _strictPathEnabled = strictPathEnabled;
        }

        @Override
        public ApiServer<T> getServer() { return _server; }

        @Override
        public Boolean isStrictPathEnabled() {
            return _strictPathEnabled;
        }
    }

    public static abstract class StaticContent<T> implements RequestHandler<T> {
        public abstract Response onRequest(final String host, String url, String filepath);

        private ApiServer<T> _server;
        public StaticContent(final ApiServer<T> apiServer) { _server = apiServer; }

        private Boolean _strictPathEnabled = false;
        public void setStrictPathEnabled(final Boolean strictPathEnabled) {
            _strictPathEnabled = strictPathEnabled;
        }

        @Override
        public ApiServer<T> getServer() { return _server; }

        @Override
        public Boolean isStrictPathEnabled() {
            return _strictPathEnabled;
        }
    }

    private Map<String, HttpHandler> _endpoints = new HashMap<String, HttpHandler>();

    private HttpServer _server;
    private Integer _port = 80;
    private Boolean _disableHttp = false;

    private HttpServer _tlsServer;
    private Integer _tlsPort = 443;
    private Boolean _useEncryption = false;
    private Boolean _redirectToTls = false;
    private String _certificateFile = null;
    private String _certificateKeyFile = null;

    private Map<String, T> _authorizationTokens = new HashMap<String, T>();

    private RequestHandler<T> _defaultEndpoint = new StaticContent<T>(this) {
        @Override
        public Response onRequest(final String host, final String uri, final String filename) {
            final Response response = new Response();
            response.setCode(Response.ResponseCodes.NOT_FOUND);
            response.setContent(new JsonResult(false, "Not found.").toJson().toString());
            return response;
        }
    };

    private RequestHandler<T> _encryptionRedirectEndpoint = new StaticContent<T>(this) {
        @Override
        public Response onRequest(final String host, final String uri, final String filename) {
            final Response response = new Response();
            final Integer standardHttpsPort = 443;

            String newUrl;
            {
                if (! _tlsPort.equals(standardHttpsPort)) {
                    newUrl = host +":"+ _tlsPort + uri;
                }
                else {
                    newUrl = host + uri;
                }

            }

            response.addHeader("Location", "https://"+ newUrl);
            response.setCode(Response.ResponseCodes.MOVED_PERMANENTLY);
            response.setContent(new JsonResult(false, "Location: https://"+ newUrl).toJson().toString());
            return response;
        }
    };

    private void _applyEndpoints(final HttpServer httpServer) {
        for (final String endpointUri : _endpoints.keySet()) {
            final com.sun.net.httpserver.HttpHandler handler = _endpoints.get(endpointUri);
            httpServer.createContext(endpointUri, handler);
        }

        if (! _endpoints.containsKey("/")) {
            httpServer.createContext("/", new HttpHandler<T>(_defaultEndpoint));
        }
    }

    /**
     * Define a RequestHandler to fulfill any requests matched by the endpoint.
     *  Setting this value after ApiServer.start() has been invoked will have no effect.
     * @param endpoint  - The string endpoint. (i.e. "/", or "/api")
     * @param handler   - The handler to fulfill the request.
     */
    public void addEndpoint(final String endpoint, final RequestHandler<T> handler) {
        _endpoints.put(endpoint, new HttpHandler<T>(handler));
    }

    /**
     * Disables all non-encrypted (HTTP) requests.
     *  Disabling HTTP will also disable any redirection set by ApiServer.redirectToTls().
     *  Setting this value after ApiServer.start() has been invoked will have no effect.
     */
    public void disableHttp(final Boolean disableHttp) { _disableHttp = disableHttp; }

    /**
     * Sets the HTTP port for the server to listen on.
     *  The default value is 80.
     *  Setting this value after ApiServer.start() has been invoked will have no effect.
     */
    public void setPort(final Integer port) { _port = port; }

    /**
     * Sets the HTTPS port for the server to listen on.
     *  The default value is 443.
     *  Setting this value after ApiServer.start() has been invoked will have no effect.
     */
    public void setTlsPort(final Integer tlsPort) { _tlsPort = tlsPort; }

    /**
     * Enables HTTPS for this server.
     *  A TLS certificate and key must be set before ApiServer.start() is invoked.
     *  To enable HTTPS, this value, and a key/certificate, must be set before ApiServer.start() is invoked.
     *  TLSv1.1 and TLSv1.2 are the only enabled protocols.
     */
    public void enableEncryption(final Boolean useEncryption) { _useEncryption = useEncryption; }

    /**
     * Sets the TLS certificate and key used to encryption.
     *  To enable encryption, this function and ApiServer.enableEncryption() must be invoked before ApiServer.start() is invoked.
     * @param tlsCertificateFile    - The path to the certificate file.
     * @param tlsCertificateKeyFile - The path to the unencrypted key file. This key must be in PKCS12, which is a binary file.
     *                                Use ./ssl/pem2pkcs.sh to convert an RSA key to PKCS12.
     */
    public void setCertificate(final String tlsCertificateFile, final String tlsCertificateKeyFile) {
        _certificateFile = tlsCertificateFile;
        _certificateKeyFile = tlsCertificateKeyFile;
    }

    /**
     * Enabling ApiServer.redirectToTls() will redirect all HTTP requests to HTTPS via a 301 Header.
     *  Encryption and HTTP must be enabled to allow this feature.
     */
    public void redirectToTls(final Boolean forceEncryption) { _redirectToTls = forceEncryption; }

    public Boolean start() {
        try {
            final Integer maxQueue = 256;
            final Executor executor = Executors.newFixedThreadPool(maxQueue);

            if (_useEncryption) {
                final HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(_tlsPort), maxQueue);
                final TlsCertificate tlsCertificate = TlsFactory.loadTlsCertificate(new String(Util.getFileContents(_certificateFile), "UTF-8"), Util.getFileContents(_certificateKeyFile));
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
                _server = HttpServer.create(new InetSocketAddress(_port), maxQueue);

                if (_redirectToTls) {
                    _server.createContext("/", new HttpHandler<T>(_encryptionRedirectEndpoint));
                }
                else {
                    _applyEndpoints(_server);
                }

                _server.setExecutor(executor);
                _server.start();
            }

            return true;
        }
        catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stop() {
        _server.stop(0);
        if (_tlsServer != null) {
            _tlsServer.stop(0);
        }
    }

    public T getAuthorizationObject(final String authorizationToken) {
        return _authorizationTokens.get(authorizationToken);
    }

    public void setAuthorizationObject(final String authToken, final T authorizationObject) {
        _authorizationTokens.put(authToken, authorizationObject);
    }
}
