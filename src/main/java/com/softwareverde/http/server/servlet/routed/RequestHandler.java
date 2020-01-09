package com.softwareverde.http.server.servlet.routed;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;

import java.util.Map;

public interface RequestHandler<E extends Environment> {
    Response handleRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception;
}
