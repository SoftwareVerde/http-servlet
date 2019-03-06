package com.softwareverde.http.server;

import com.softwareverde.http.server.servlet.response.JsonResponse;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.json.Json;

class Util extends com.softwareverde.util.Util {
    protected Util() { }

    public static JsonResponse createJsonErrorResponse(final Integer responseCode, final String errorMessage) {
        final Json json = new Json();
        json.put("wasSuccess", false);
        json.put("errorCode", responseCode);
        json.put("errorMessage", errorMessage);
        return new JsonResponse(Response.Codes.SERVER_ERROR, json);
    }
}
