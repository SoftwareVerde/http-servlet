package com.softwareverde.http.server;

import com.softwareverde.http.websocket.ConnectionLayer;
import com.softwareverde.http.websocket.WebSocket;

public interface WebSocketFactory {
    default WebSocket newWebSocket(final Long webSocketId, final WebSocket.Mode mode, final ConnectionLayer connectionLayer, final Integer maxPacketByteCount) {
        return new WebSocket(webSocketId, mode, connectionLayer, maxPacketByteCount);
    }
}
