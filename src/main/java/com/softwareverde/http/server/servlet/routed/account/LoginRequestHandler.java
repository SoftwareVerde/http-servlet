package com.softwareverde.http.server.servlet.routed.account;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.routed.RequestHandler;
import com.softwareverde.http.server.servlet.routed.json.JsonRequestHandler;
import com.softwareverde.http.server.servlet.session.Session;
import com.softwareverde.http.server.servlet.session.SessionManager;
import com.softwareverde.json.Json;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class LoginRequestHandler<E extends Environment> implements RequestHandler<E> {
    public static final String ACCOUNT_SESSION_KEY = "account";

    private final SessionManager _sessionManager;
    private final Authenticator<E> _authenticator;

    public LoginRequestHandler(final SessionManager sessionManager, final Authenticator<E> authenticator) {
        _sessionManager = sessionManager;
        _authenticator = authenticator;
    }

    @Override
    public Response handleRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception {
        final Response response = new Response();

        final AuthenticationResult authenticationResult = _authenticator.authenticateUser(request, environment, parameters);
        if (! authenticationResult.wasSuccess()) {
            response.setCode(HttpServletResponse.SC_UNAUTHORIZED);
            final Json responseContent = JsonRequestHandler.generateErrorJson(authenticationResult.getErrorMessage());
            response.setContent(responseContent.toString());
            return response;
        }

        final Session session = _sessionManager.createSession(request, response);
        final Json sessionData = session.getMutableData();
        sessionData.put(ACCOUNT_SESSION_KEY, authenticationResult.getAccountJson());
        _sessionManager.saveSession(session);

        response.setCode(HttpServletResponse.SC_OK);
        final Json responseContent = JsonRequestHandler.generateSuccessJson();
        response.setContent(responseContent.toString());
        return response;
    }
}
