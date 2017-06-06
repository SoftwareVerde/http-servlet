package com.softwareverde.httpserver.endpoint;

import com.softwareverde.httpserver.HttpServer;

public abstract class Endpoint implements HttpServer.RequestHandler {
    public Endpoint() {  }

    private Boolean _strictPathEnabled = false;
    public void setStrictPathEnabled(final Boolean strictPathEnabled) {
        _strictPathEnabled = strictPathEnabled;
    }

    @Override
    public Boolean isStrictPathEnabled() {
        return _strictPathEnabled;
    }
}