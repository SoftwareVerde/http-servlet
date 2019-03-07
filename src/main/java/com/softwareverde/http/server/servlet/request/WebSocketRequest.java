package com.softwareverde.http.server.servlet.request;

import com.softwareverde.http.server.servlet.response.Response;

import java.util.List;

public class WebSocketRequest extends Request {
    public String getWebSocketKey() {
        if (! _headers.containsHeader(Response.Headers.WebSocket.KEY)) { return null; }

        final List<String> headerValues = _headers.getHeader(Response.Headers.WebSocket.KEY);
        if (headerValues.isEmpty()) { return null; }

        final String headerValue = headerValues.get(0);
        if (headerValue == null) { return null; }
        if (headerValue.isEmpty()) { return null; }

        return headerValue;
    }

    public Boolean isWebSocketRequest() {
        for (final String headerKey : _headers.getHeaderNames()) {
            for (final String headerValue : _headers.getHeader(headerKey)) {
                if (Request.isWebSocketHeader(headerKey, headerValue)) { return true; }
            }
        }

        return false;
    }
}
