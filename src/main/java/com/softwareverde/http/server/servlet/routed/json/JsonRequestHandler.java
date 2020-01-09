package com.softwareverde.http.server.servlet.routed.json;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.routed.RequestHandler;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.logging.LoggerInstance;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class JsonRequestHandler<E extends Environment> implements RequestHandler<E> {
    private LoggerInstance _logger = Logger.getInstance(getClass());

    public static final String JSON_SUCCESS_FIELD = "wasSuccess";
    public static final String JSON_ERROR_FIELD = "errorMessage";

    @Override
    public Response handleRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception {
        final Response response = new Response();
        response.addHeader(Response.Headers.CONTENT_TYPE, "application/json");

        Json json;
        try {
            json = handleJsonRequest(request, environment, parameters);

            if (json == null) {
                json = JsonRequestHandler.generateErrorJson("Unable to determine response");
            }
        }
        catch (final Exception e) {
            String msg = "Unable to handle request";
            _logger.error(msg, e);
            json = JsonRequestHandler.generateErrorJson(msg + ": " + e.getMessage());
        }

        response.setCode(HttpServletResponse.SC_OK);
        response.setContent(json.toString());
        return response;
    }

    protected abstract Json handleJsonRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception;

    public static Json getRequestDataAsJson(final Request request) throws IOException {
        final byte[] rawData = request.getRawPostData();
        String messageBody = new String(rawData);
        return Json.parse(messageBody);
    }

    public static void setJsonSuccessFields(Json json) {
        json.put(JSON_SUCCESS_FIELD, true);
        json.put(JSON_ERROR_FIELD, null);
    }

    public static Json generateSuccessJson() {
        Json json = new Json();
        json.put(JSON_SUCCESS_FIELD, true);
        json.put(JSON_ERROR_FIELD, null);
        return json;
    }

    public static Json generateErrorJson(String errorMessage) {
        Json json = new Json();
        json.put(JSON_SUCCESS_FIELD, false);
        json.put(JSON_ERROR_FIELD, errorMessage);
        return json;
    }
}
