package com.softwareverde.httpserver;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.cookie.CookieParser;
import com.softwareverde.logging.Log;
import com.softwareverde.servlet.WebSocketServlet;
import com.softwareverde.servlet.request.RequestInflater;
import com.softwareverde.servlet.request.WebSocketRequest;
import com.softwareverde.servlet.response.Response;
import com.softwareverde.servlet.response.WebSocketResponse;
import com.softwareverde.servlet.socket.ConnectionLayer;
import com.softwareverde.servlet.socket.WebSocket;
import com.softwareverde.util.ReflectionUtil;
import com.softwareverde.util.StringUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
                response = Util.createJsonErrorResponse(Response.ResponseCodes.NOT_FOUND, "Not found.");
            }
            else {
                final RequestInflater requestInflater = new RequestInflater();
                final WebSocketRequest webSocketRequest = requestInflater.createWebSocketRequest(httpExchange);
                if (webSocketRequest == null) {
                    response = Util.createJsonErrorResponse(Response.ResponseCodes.SERVER_ERROR, "Bad request.");
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
                        Log.error(this.getClass(), "Error handling request: " + httpExchange.getRequestURI(), exception);

                        shouldUpgradeToWebSocket = false;
                        webSocketKey = null;
                        webSocketId = null;
                        response = Util.createJsonErrorResponse(Response.ResponseCodes.SERVER_ERROR, "Server error.");
                    }
                }
            }
        }

        DelayedOutputStream delayedOutputStream = null;
        ConnectionLayer connectionLayer = null;
        try {
            // HttpExchange does not behave correctly when the 101 ResponseCode is used to upgrade the connection;
            //  the most prominent issue is that it sends a Content-Length: 0 header, which causes the client to close.
            //  Therefore, we access the rawInputStream ("ris") and rawOutputStream ("ros") of the underlying implementation
            //  and trick the HttpExchange into behaving correctly.
            // HttpExchange Source: http://www.docjar.com/html/api/sun/net/httpserver/ExchangeImpl.java.html
            final Object httpExchangeImplementation = ReflectionUtil.getValue(httpExchange, "impl");
            final Object httpConnection = ReflectionUtil.getValue(httpExchangeImplementation, "connection");

            // final SSLEngine sslEngine = ReflectionUtil.invoke(httpConnection, "getSSLEngine");
            InputStream sslInputStream = null;
            OutputStream sslOutputStream = null;
            final Object sslStreams = ReflectionUtil.getValue(httpConnection, "sslStreams");
            if (sslStreams != null) {
                sslInputStream = ReflectionUtil.invoke(sslStreams, "getInputStream");
                sslOutputStream = ReflectionUtil.invoke(sslStreams, "getOutputStream");
            }

            final SocketChannel socketChannel = ReflectionUtil.getValue(httpConnection, "chan");
            final Socket rawSocket = socketChannel.socket();
            final InputStream rawInputStream = rawSocket.getInputStream();
            final OutputStream rawOutputStream = rawSocket.getOutputStream();

            delayedOutputStream = new DelayedOutputStream(sslOutputStream != null ? sslOutputStream : rawOutputStream);
            ReflectionUtil.setValue(httpExchangeImplementation, "ros", delayedOutputStream);

            if ( (sslInputStream != null) && (sslOutputStream != null)) {
                connectionLayer = ConnectionLayer.newSecureConnectionLayer(rawSocket, sslInputStream, sslOutputStream);
            }
            else {
                connectionLayer = ConnectionLayer.newConnectionLayer(rawSocket);
            }
        }
        catch (final Exception exception) {
            Log.error("Error initializing Web Socket.", exception);
        }

        if (connectionLayer == null) {
            response = Util.createJsonErrorResponse(Response.ResponseCodes.SERVER_ERROR, "Server error.  Unable to initialize Web Socket.");
            shouldUpgradeToWebSocket = false;
        }

        if (shouldUpgradeToWebSocket) { // Update the Response for the WebSocket Upgrade...
            // NOTE: Faking a 200 ResponseCode so that the HttpExchange behaves correctly.  The correct ResponseCode is replaced later.
            //  Setting the ResponseCode to OK prevents a content-length header from being sent and also does not close the streams.
            response.setCode(Response.ResponseCodes.OK);
            response.setHeader(Response.Headers.UPGRADE, Response.Headers.WebSocket.Values.UPGRADE);
            response.setHeader(Response.Headers.CONNECTION, Response.Headers.WebSocket.Values.CONNECTION);
            response.setHeader(Response.Headers.WebSocket.ACCEPT, Response.Headers.WebSocket.Values.createAcceptHeader(webSocketKey));
        }

        if (delayedOutputStream != null) {
            if (shouldUpgradeToWebSocket) {
                delayedOutputStream.delay();
            }
            else {
                delayedOutputStream.resume();
            }
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

        if (shouldUpgradeToWebSocket) {
            { // Hack the HttpExchange to work around the HTTP 101 bug that closes the socket with a Content-Length header...
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
        }
        else {
            final byte[] responseBytes = response.getContent();
            httpExchange.sendResponseHeaders(response.getCode(), (responseBytes == null ? -1 : responseBytes.length));

            if (responseBytes != null) {
                final OutputStream outputStream = httpExchange.getResponseBody();
                outputStream.write(responseBytes);
                outputStream.flush();
                outputStream.close();
            }

            httpExchange.close();
        }
    }
}
