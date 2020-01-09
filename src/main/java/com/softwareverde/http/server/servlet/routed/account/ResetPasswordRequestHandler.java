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

public class ResetPasswordRequestHandler<E extends Environment> implements RequestHandler<E> {
    private final SessionManager _sessionManager;
    private final Authenticator<E> _authenticator;

    public ResetPasswordRequestHandler(final SessionManager sessionManager, final Authenticator<E> authenticator) {
        _sessionManager = sessionManager;
        _authenticator = authenticator;
    }

    @Override
    public Response handleRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception {
        final Response response = new Response();

        final Session session = _sessionManager.getSession(request);
        if (session == null) {
            response.setCode(HttpServletResponse.SC_UNAUTHORIZED);
            final Json responseContent = JsonRequestHandler.generateErrorJson("Unauthorized.");
            response.setContent(responseContent.toString());
            return response;
        }

        final Json sessionData = session.getMutableData();
        if (! sessionData.hasKey(LoginRequestHandler.ACCOUNT_SESSION_KEY)) {
            response.setCode(HttpServletResponse.SC_UNAUTHORIZED);
            final Json responseContent = JsonRequestHandler.generateErrorJson("Unauthorized.");
            response.setContent(responseContent.toString());
            return response;
        }

        final ResetPasswordResult resetPasswordResult = _authenticator.resetPassword(session, request, environment, parameters);
        if (! resetPasswordResult.wasSuccess()) {
            response.setCode(HttpServletResponse.SC_UNAUTHORIZED);
            final Json responseContent = JsonRequestHandler.generateErrorJson(resetPasswordResult.getErrorMessage());
            response.setContent(responseContent.toString());
            return response;
        }

        response.setCode(HttpServletResponse.SC_OK);
        final Json responseContent = JsonRequestHandler.generateSuccessJson();
        response.setContent(responseContent.toString());
        return response;
    }
}
