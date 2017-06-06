package com.softwareverde.httpserver.endpoint;

import com.softwareverde.httpserver.request.Request;
import com.softwareverde.httpserver.response.JsonResult;
import com.softwareverde.httpserver.response.Response;

public class NotFoundJsonEndpoint extends StaticContentHandler {
    public NotFoundJsonEndpoint() { }

    @Override
    public Response onRequest(final Request request) {
        final Response response = new Response();
        response.setCode(Response.ResponseCodes.NOT_FOUND);
        response.setContent(new JsonResult(false, "Not found.").toJson().toString());
        return response;
    }
}
