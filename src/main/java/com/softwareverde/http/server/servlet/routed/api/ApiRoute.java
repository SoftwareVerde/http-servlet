package com.softwareverde.http.server.servlet.routed.api;

import java.util.Map;

public class ApiRoute<T> {
    private Map<String, String> _parameters;
    private T _requestHandler;

    protected ApiRoute(final T requestHandler, final Map<String, String> parameters) {
        this._requestHandler = requestHandler;
        this._parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return _parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this._parameters = parameters;
    }

    public T getRequestHandler() {
        return _requestHandler;
    }

    public void setRequestHandler(final T requestHandler) {
        this._requestHandler = requestHandler;
    }
}
