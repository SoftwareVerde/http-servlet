package com.softwareverde.http.server.servlet.routed.account;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.routed.RequestHandler;
import com.softwareverde.http.server.servlet.session.SessionManager;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class LogoutRequestHandler<E extends Environment> implements RequestHandler<E> {
    private final SessionManager _sessionManager;

    public LogoutRequestHandler(final SessionManager sessionManager) {
        _sessionManager = sessionManager;
    }

    @Override
    public Response handleRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception {
        final Response response = new Response();

        if (_sessionManager.destroySession(request, response)) {
            return response;
        }
        else {
            response.setCode(HttpServletResponse.SC_RESET_CONTENT);
            return response;
        }
    }
}
