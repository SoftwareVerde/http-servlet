package com.softwareverde.http.server.servlet;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;

public class NotFoundJsonServlet implements Servlet {
    public NotFoundJsonServlet() { }

    @Override
    public Response onRequest(final Request request) {
        final Response response = new Response();
        response.setCode(Response.Codes.NOT_FOUND);
        response.setContent("Not found.");
        return response;
    }
}
