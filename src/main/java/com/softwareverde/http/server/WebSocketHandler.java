package com.softwareverde.http.server;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.cookie.CookieParser;
import com.softwareverde.http.server.servlet.WebSocketServlet;
import com.softwareverde.http.server.servlet.request.RequestInflater;
import com.softwareverde.http.server.servlet.request.WebSocketRequest;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.response.WebSocketResponse;
import com.softwareverde.http.websocket.ConnectionLayer;
import com.softwareverde.http.websocket.WebSocket;
import com.softwareverde.logging.Log;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.ReflectionUtil;
import com.softwareverde.util.StringUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

class WebSocketHandler implements com.sun.net.httpserver.HttpHandler {
    protected final WebSocketServlet _servlet;
    protected final Boolean _shouldUseStrictPathMatching;
    protected final Integer _maxPacketByteCount;

    public WebSocketHandler(final WebSocketServlet servlet, final Boolean shouldUseStrictPathMatching) {
        this(servlet, shouldUseStrictPathMatching, 8192);
    }

    public WebSocketHandler(final WebSocketServlet servlet, final Boolean shouldUseStrictPathMatching, final Integer maxPacketByteCount) {
        _servlet = servlet;
        _shouldUseStrictPathMatching = shouldUseStrictPathMatching;
        _maxPacketByteCount = maxPacketByteCount;
    }

    public WebSocketServlet getServlet() {
        return _servlet;
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final Boolean pathIsStrictMatch = HttpHandler.isPathStrictlyMatched(httpExchange);

        Boolean shouldUpgradeToWebSocket = false;
        String webSocketKey = null;
        Long webSocketId = null;
        Response response;
        {
            if ( (_shouldUseStrictPathMatching) && (! pathIsStrictMatch) ) {
                response = Util.createJsonErrorResponse(Response.Codes.NOT_FOUND, "Not found.");
            }
            else {
                final RequestInflater requestInflater = new RequestInflater();
                final WebSocketRequest webSocketRequest = requestInflater.createWebSocketRequest(httpExchange);
                if (webSocketRequest == null) {
                    response = Util.createJsonErrorResponse(Response.Codes.SERVER_ERROR, "Bad request.");
                }
                else {
                    try {
                        final WebSocketResponse webSocketResponse = _servlet.onRequest(webSocketRequest);

                        shouldUpgradeToWebSocket = webSocketResponse.shouldUpgradeToWebSocket();
                        webSocketKey = (shouldUpgradeToWebSocket ? webSocketRequest.getWebSocketKey() : null);
                        webSocketId = (shouldUpgradeToWebSocket ? webSocketResponse.getWebSocketId() : null);
                        response = webSocketResponse;
                    }
                    catch (final Exception exception) {
                        Logger.warn(WebSocketHandler.class, "Error handling request: " + httpExchange.getRequestURI(), exception);

                        shouldUpgradeToWebSocket = false;
                        webSocketKey = null;
                        webSocketId = null;
                        response = Util.createJsonErrorResponse(Response.Codes.SERVER_ERROR, "Server error.");
                    }
                }
            }
        }

        DelayedOutputStream delayedOutputStream = null;
        ConnectionLayer connectionLayer = null;
        try {
            Logger.info("I'm trying");
            final InputStream inputStream = httpExchange.getRequestBody();
            Logger.info("input steam: " + inputStream.getClass());
            final OutputStream outputStream = httpExchange.getResponseBody();
            Logger.info("output steam: " + outputStream.getClass());

            final SocketImpl socketImpl = new DummySocketImpl(inputStream, outputStream);
            Logger.info("socketImpl: " + socketImpl.getClass());
            final Socket dummySocket = new Socket(socketImpl){
                @Override
                public boolean isConnected() {
                    return true;
                }

                @Override
                public boolean isBound() {
                    return true;
                }
            };
            
            // // HttpExchange does not behave correctly when the 101 ResponseCode is used to upgrade the connection;
            // //  the most prominent issue is that it sends a Content-Length: 0 header, which causes the client to close.
            // //  Therefore, we access the rawInputStream ("ris") and rawOutputStream ("ros") of the underlying implementation
            // //  and trick the HttpExchange into behaving correctly.
            // // HttpExchange Source: http://www.docjar.com/html/api/sun/net/httpserver/ExchangeImpl.java.html
            // final Object httpExchangeImplementation = ReflectionUtil.getValue(httpExchange, "impl");
            // final Object httpConnection = ReflectionUtil.getValue(httpExchangeImplementation, "connection");

            // // final SSLEngine sslEngine = ReflectionUtil.invoke(httpConnection, "getSSLEngine");
            // InputStream sslInputStream = null;
            // OutputStream sslOutputStream = null;
            // final Object sslStreams = ReflectionUtil.getValue(httpConnection, "sslStreams");
            // if (sslStreams != null) {
            //     sslInputStream = ReflectionUtil.invoke(sslStreams, "getInputStream");
            //     sslOutputStream = ReflectionUtil.invoke(sslStreams, "getOutputStream");
            // }

            // final SocketChannel socketChannel = ReflectionUtil.getValue(httpConnection, "chan");
            // final Socket rawSocket = dummySocket;
            // final InputStream rawInputStream = inputStream;
            // final OutputStream rawOutputStream = outputStream;

            delayedOutputStream = new DelayedOutputStream(outputStream);
           // ReflectionUtil.setValue(httpExchangeImplementation, "ros", delayedOutputStream);
            httpExchange.setStreams(null, delayedOutputStream);

            final URI uri = httpExchange.getRequestURI();
            final boolean isHttps = Util.areEqual("https", uri.getScheme());
            Logger.info("isHttps " + isHttps);
            if (isHttps) {
                connectionLayer = ConnectionLayer.newSecureConnectionLayer(dummySocket, inputStream, outputStream);
            }
            else {
                connectionLayer = ConnectionLayer.newConnectionLayer(dummySocket);
            }
        }
        catch (final Exception exception) {
            Logger.error(WebSocketHandler.class, "Error initializing Web Socket.", exception);
        }

        if (connectionLayer == null) {
            response = Util.createJsonErrorResponse(Response.Codes.SERVER_ERROR, "Server error.  Unable to initialize Web Socket.");
            shouldUpgradeToWebSocket = false;
            Logger.info("connectionLayer is null");
        }

        if (shouldUpgradeToWebSocket) { // Update the Response for the WebSocket Upgrade...
            // NOTE: Faking a 200 ResponseCode so that the HttpExchange behaves correctly.  The correct ResponseCode is replaced later.
            //  Setting the ResponseCode to OK prevents a content-length header from being sent and also does not close the streams.
            response.setCode(Response.Codes.OK);
            response.setHeader(Response.Headers.UPGRADE, Response.Headers.WebSocket.Values.UPGRADE);
            response.setHeader(Response.Headers.CONNECTION, Response.Headers.WebSocket.Values.CONNECTION);
            response.setHeader(Response.Headers.WebSocket.ACCEPT, Response.Headers.WebSocket.Values.createAcceptHeader(webSocketKey));
            Logger.info("in shouldUpgradeToWebSocket");
        }
        Logger.info("after shouldUpgradeToWebSocket");
        if (delayedOutputStream != null) {
            if (shouldUpgradeToWebSocket) {
                delayedOutputStream.delay();
            }
            else {
                delayedOutputStream.resume();
            }
            Logger.info("delayedOutputStream is not null");
        }

        { // Send Response Headers...
            final Headers httpExchangeHeaders = httpExchange.getResponseHeaders();
            final Map<String, List<String>> compiledHeaders = response.getHeaders();
            for (final String headerKey : compiledHeaders.keySet()) {
                final List<String> headerValues = compiledHeaders.get(headerKey);
                for (final String headerValue : headerValues) {
                    httpExchangeHeaders.add(headerKey, headerValue);
                }
            }

            final List<Cookie> cookies = response.getCookies();
            final CookieParser cookieParser = new CookieParser();
            final List<String> compiledCookies = cookieParser.compileCookiesIntoSetCookieHeaderValues(cookies);
            for (final String setCookieHeader : compiledCookies) {
                httpExchangeHeaders.add(Response.Headers.SET_COOKIE, setCookieHeader);
            }
        }
        Logger.info("response header sent");

        if (shouldUpgradeToWebSocket) {
            { // Hack the HttpExchange to work around the HTTP 101 bug that closes the socket with a Content-Length header...
                Logger.info("in shouldUpgradeToWebSocket if");
                httpExchange.sendResponseHeaders(response.getCode(), 0);
                final String payload = StringUtil.bytesToString(delayedOutputStream.getDelayedPayload());
                final String newPayload = payload.replaceFirst("HTTP/1.1 200[^\\r\\n]*\\r\\n", "HTTP/1.1 101 Switching Protocols\r\n");
                delayedOutputStream.clearDelayedPayload();
                delayedOutputStream.resume();
                delayedOutputStream.write(StringUtil.stringToBytes(newPayload));
                delayedOutputStream.flush();
            }

            { // Configure the raw socket...
                final Socket socket = connectionLayer.getSocket();
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
            }

            final WebSocket webSocket = new WebSocket(webSocketId, WebSocket.Mode.SERVER, connectionLayer, _maxPacketByteCount);
            _servlet.onNewWebSocket(webSocket);
            Logger.info("Almost done");
        }
        else {
            Logger.info("in shouldUpgradeToWebSocket else");
            final byte[] responseBytes = response.getContent();
            httpExchange.sendResponseHeaders(response.getCode(), (responseBytes == null ? -1 : responseBytes.length));
            Logger.info("after sendResponseHeaders");

            if (responseBytes != null) {
                Logger.info("is not null");
                final OutputStream outputStream = httpExchange.getResponseBody();
                Logger.info("initialized " + outputStream.getClass());
                try{
                    outputStream.write(responseBytes);
                }catch (Exception exception){
                    Logger.error(exception);
                    throw exception;
                }
                Logger.info("after write");
                outputStream.flush();
                outputStream.close();
            }
            Logger.info("I tried");
            httpExchange.close();
        }
    }
}
