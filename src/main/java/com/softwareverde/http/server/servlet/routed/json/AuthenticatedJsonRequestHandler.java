package com.softwareverde.http.server.servlet.routed.json;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.AuthenticatedRequestHandler;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.session.Session;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.logging.LoggerInstance;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class AuthenticatedJsonRequestHandler<E extends Environment> implements AuthenticatedRequestHandler<E> {
    private final LoggerInstance _logger = Logger.getInstance(getClass());

    @Override
    public Response handleRequest(final Session session, final Request request, final E environment, final Map<String, String> parameters) throws Exception {
        final Response response = new Response();
        response.addHeader(Response.Headers.CONTENT_TYPE, "application/json");

        Json json;
        try {
            json = handleJsonRequest(session, request, environment, parameters);

            if (json == null) {
                json = AuthenticatedJsonRequestHandler.generateErrorJson("Unable to determine response");
            }
        }
        catch (final Exception e) {
            String msg = "Unable to handle request";
            _logger.error(msg, e);
            json = AuthenticatedJsonRequestHandler.generateErrorJson(msg + ": " + e.getMessage());
        }

        response.setCode(HttpServletResponse.SC_OK);
        response.setContent(json.toString());
        return response;
    }

    protected abstract Json handleJsonRequest(final Session session, final Request request, final E environment, final Map<String, String> parameters) throws Exception;

    public static Json getRequestDataAsJson(final Request request) throws IOException {
        return JsonRequestHandler.getRequestDataAsJson(request);
    }

    public static void setJsonSuccessFields(Json json) {
        JsonRequestHandler.setJsonSuccessFields(json);
    }

    public static Json generateSuccessJson() {
        return JsonRequestHandler.generateSuccessJson();
    }

    public static Json generateErrorJson(String errorMessage) {
        return JsonRequestHandler.generateErrorJson(errorMessage);
    }
}
