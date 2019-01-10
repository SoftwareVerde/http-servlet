package com.softwareverde.httpserver;

import com.softwareverde.json.Json;
import com.softwareverde.servlet.response.JsonResponse;
import com.softwareverde.servlet.response.Response;

class Util {
    protected Util() { }

    public static JsonResponse createJsonErrorResponse(final Integer responseCode, final String errorMessage) {
        final Json json = new Json();
        json.put("wasSuccess", false);
        json.put("errorCode", responseCode);
        json.put("errorMessage", errorMessage);
        return new JsonResponse(Response.ResponseCodes.SERVER_ERROR, json);
    }
}
