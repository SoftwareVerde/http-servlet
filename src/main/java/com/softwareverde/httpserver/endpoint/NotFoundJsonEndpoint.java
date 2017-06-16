package com.softwareverde.httpserver.endpoint;

import com.softwareverde.servlet.Servlet;
import com.softwareverde.servlet.request.Request;
import com.softwareverde.servlet.response.Response;

public class NotFoundJsonEndpoint implements Servlet {
    public NotFoundJsonEndpoint() { }

    @Override
    public Response onRequest(final Request request) {
        final Response response = new Response();
        response.setCode(Response.ResponseCodes.NOT_FOUND);
        response.setContent("Not found.");
        return response;
    }
}
