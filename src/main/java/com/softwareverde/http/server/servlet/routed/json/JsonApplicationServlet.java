package com.softwareverde.http.server.servlet.routed.json;

import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.ApplicationServlet;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.json.Json;

/**
 * <p>This class overrides the error message methods from {@link ApplicationServlet} to ensure the default errors returned
 * from this routed are Json errors.</p>
 *
 * <p>Note that this does not limit what kinds of handlers can be added to the routed.  While generally it is expected that
 * {@link JsonRequestHandler} objects will be used, it is still possible to add any other kind of handler to the routed to
 * handle certain types of requests.  This does, however, mean that if an error were to occur while routing to such a handler
 * (or if the handler throws an exception), the message returned would then be JSON.</p>
 * @param <E>
 */
public class JsonApplicationServlet<E extends Environment> extends ApplicationServlet<E> {

    public JsonApplicationServlet(final E environment) {
        super(environment);
    }

    public JsonApplicationServlet(final E environment, final String apiBaseUrl) {
        super(environment, apiBaseUrl);
    }

    public JsonApplicationServlet(final E environment, final String apiBaseUrl, final JsonRequestHandler<E> errorApiRoute) {
        super(environment, apiBaseUrl, errorApiRoute);
    }

    @Override
    protected Response _getBadRequestResponse() {
        final Response response = super._getBadRequestResponse();
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
