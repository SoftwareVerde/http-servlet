package com.softwareverde.httpserver.endpoint;

import com.softwareverde.httpserver.HttpServer;
import com.softwareverde.httpserver.request.Request;
import com.softwareverde.httpserver.response.Response;

public abstract class StaticContentHandler implements HttpServer.RequestHandler {
    public abstract Response onRequest(Request request);

    public StaticContentHandler() { }

    private Boolean _strictPathEnabled = false;
    public void setStrictPathEnabled(final Boolean strictPathEnabled) {
        _strictPathEnabled = strictPathEnabled;
    }

    @Override
    public Boolean isStrictPathEnabled() {
        return _strictPathEnabled;
    }
}