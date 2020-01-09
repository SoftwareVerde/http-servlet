package com.softwareverde.http.server.servlet.routed.api;

public class RouteNotFoundException extends Exception {
    public RouteNotFoundException() {}

    public RouteNotFoundException(final String message) {
        super(message);
    }

    public RouteNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
