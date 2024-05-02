package com.softwareverde.http.server.endpoint;

import com.softwareverde.http.server.WebSocketFactory;
import com.softwareverde.http.server.servlet.WebSocketServlet;

public class WebSocketEndpoint {
    protected final WebSocketServlet _servlet;
    protected String _path;
    protected Boolean _shouldUseStrictPath;

    protected WebSocketFactory _webSocketFactory;

    public WebSocketEndpoint(final WebSocketServlet servlet) {
        this(servlet, null);
    }

    public WebSocketEndpoint(final WebSocketServlet servlet, final WebSocketFactory webSocketFactory) {
        _servlet = servlet;
        _webSocketFactory = webSocketFactory;
    }

    public void setPath(final String path) {
        _path = path;
    }

    public String getPath() {
        return _path;
    }

    public void setStrictPathEnabled(final Boolean strictPathEnabled) {
        _shouldUseStrictPath = strictPathEnabled;
    }

    public Boolean shouldUseStrictPath() {
        return _shouldUseStrictPath;
    }

    public WebSocketServlet getServlet() {
        return _servlet;
    }

    public WebSocketFactory getWebSocketFactory() {
        return _webSocketFactory;
    }
}
