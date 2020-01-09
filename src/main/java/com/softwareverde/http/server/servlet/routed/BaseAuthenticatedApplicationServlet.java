package com.softwareverde.http.server.servlet.routed;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.account.LoginRequestHandler;
import com.softwareverde.http.server.servlet.session.Session;
import com.softwareverde.http.server.servlet.session.SessionManager;
import com.softwareverde.json.Json;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public abstract class BaseAuthenticatedApplicationServlet<E extends Environment, RequestHandler extends AuthenticatedRequestHandler<E>> extends RoutedServlet<E, RequestHandler> {
    private final SessionManager _sessionManager;

    public BaseAuthenticatedApplicationServlet(final E environment, final SessionManager sessionManager) {
        super(environment);
        _sessionManager = sessionManager;
    }

    public BaseAuthenticatedApplicationServlet(final E environment, final String apiBaseUrl, final SessionManager sessionManager) {
        super(environment, apiBaseUrl);
        _sessionManager = sessionManager;
    }

    public BaseAuthenticatedApplicationServlet(final E environment, final String apiBaseUrl, final RequestHandler errorApiRoute, final SessionManager sessionManager) {
        super(environment, apiBaseUrl, errorApiRoute);
        _sessionManager = sessionManager;
    }

    @Override
    protected Response _handleRequest(final E environment, final Request request, final RequestHandler authenticatedRequestHandler, final Map<String, String> routeParameters) throws Exception {
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

    protected Response _getUnauthenticatedErrorResponse() {
        final Response response = new Response();
        response.setCode(HttpServletResponse.SC_FORBIDDEN);
        response.setContent("Unauthorized.");
        return response;
    }
}
