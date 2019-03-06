package com.softwareverde.http.server.servlet;

import com.softwareverde.http.server.servlet.request.WebSocketRequest;
import com.softwareverde.http.server.servlet.response.WebSocketResponse;
import com.softwareverde.http.websocket.WebSocket;

public interface WebSocketServlet {
    WebSocketResponse onRequest(WebSocketRequest request);
    void onNewWebSocket(final WebSocket webSocket);
}
