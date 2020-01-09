package com.softwareverde.http.server.servlet.routed;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.session.Session;

import java.util.Map;

public interface AuthenticatedRequestHandler<E extends Environment> {
    Response handleRequest(final Session session, final Request request, final E environment, final Map<String, String> parameters) throws Exception;
}
