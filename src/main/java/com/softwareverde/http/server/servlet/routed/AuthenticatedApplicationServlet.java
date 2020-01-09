package com.softwareverde.http.server.servlet.routed;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.account.LoginRequestHandler;
import com.softwareverde.http.server.servlet.session.Session;
import com.softwareverde.http.server.servlet.session.SessionManager;
import com.softwareverde.json.Json;

import java.util.Map;

public class AuthenticatedApplicationServlet<E extends Environment> extends BaseAuthenticatedApplicationServlet<E, AuthenticatedRequestHandler<E>> {
    private final SessionManager _sessionManager;

    public AuthenticatedApplicationServlet(final E environment, final SessionManager sessionManager) {
        super(environment, sessionManager);
        _sessionManager = sessionManager;
    }

    public AuthenticatedApplicationServlet(final E environment, final String apiBaseUrl, final SessionManager sessionManager) {
        super(environment, apiBaseUrl, sessionManager);
        _sessionManager = sessionManager;
    }

    public AuthenticatedApplicationServlet(final E environment, final String apiBaseUrl, final AuthenticatedRequestHandler<E> errorApiRoute, final SessionManager sessionManager) {
        super(environment, apiBaseUrl, errorApiRoute, sessionManager);
        _sessionManager = sessionManager;
    }

    @Override
    protected Response _handleRequest(final E environment, final Request request, final AuthenticatedRequestHandler<E> authenticatedRequestHandler, final Map<String, String> routeParameters) throws Exception {
        final Session session = _sessionManager.getSession(request);
        if (session == null) {
            return _getUnauthenticatedErrorResponse();
        }

        final Json sessionData = session.getMutableData();
        if (! sessionData.hasKey(LoginRequestHandler.ACCOUNT_SESSION_KEY)) {
            return _getUnauthenticatedErrorResponse();
        }

        try {
            return authenticatedRequestHandler.handleRequest(session, request, environment, routeParameters);
        }
        finally {
            // save any session changes
            _sessionManager.saveSession(session);
        }
    }
}
