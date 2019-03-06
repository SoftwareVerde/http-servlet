package com.softwareverde.http.server.servlet.response;

public class WebSocketResponse extends Response {
    protected Boolean _shouldUpgradeToWebSocket = false;
    protected Long _webSocketId = null;

    public void upgradeToWebSocket() {
        _shouldUpgradeToWebSocket = true;
    }

    public void setWebSocketId(final Long webSocketId) {
        _webSocketId = webSocketId;
    }

    public Boolean shouldUpgradeToWebSocket() {
        return _shouldUpgradeToWebSocket;
    }

    public Long getWebSocketId() {
        return (_shouldUpgradeToWebSocket ? _webSocketId : null);
    }
}
