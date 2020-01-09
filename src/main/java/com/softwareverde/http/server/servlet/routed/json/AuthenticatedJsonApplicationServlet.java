package com.softwareverde.http.server.servlet.routed.json;

import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.BaseAuthenticatedApplicationServlet;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.session.SessionManager;
import com.softwareverde.json.Json;

public class AuthenticatedJsonApplicationServlet<E extends Environment> extends BaseAuthenticatedApplicationServlet<E, AuthenticatedJsonRequestHandler<E>> {
    public AuthenticatedJsonApplicationServlet(final E environment, final SessionManager sessionManager) {
        super(environment, sessionManager);
    }

    public AuthenticatedJsonApplicationServlet(final E environment, final String apiBaseUrl, final SessionManager sessionManager) {
        super(environment, apiBaseUrl, sessionManager);
    }

    public AuthenticatedJsonApplicationServlet(final E environment, final String apiBaseUrl, final AuthenticatedJsonRequestHandler<E> errorApiRoute, final SessionManager sessionManager) {
        super(environment, apiBaseUrl, errorApiRoute, sessionManager);
    }

    @Override
    protected Response _getBadRequestResponse() {
        final Response response = super._getBadRequestResponse();
        _replaceContentWithJsonError(response);
        return response;
    }

    @Override
    protected Response _getUnauthenticatedErrorResponse() {
        final Response response = super._getUnauthenticatedErrorResponse();
        _replaceContentWithJsonError(response);
        return response;
    }

    @Override
    protected Response _getServerErrorResponse() {
        final Response response = super._getServerErrorResponse();
        _replaceContentWithJsonError(response);
        return response;
    }

    protected void _replaceContentWithJsonError(final Response response) {
        final String responseContent = new String(response.getContent());

        final Json json = JsonRequestHandler.generateErrorJson(responseContent);
        response.setContent(json.toString());
    }
}
