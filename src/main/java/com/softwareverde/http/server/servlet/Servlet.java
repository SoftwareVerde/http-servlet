package com.softwareverde.http.server.servlet;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;

public interface Servlet {
    Response onRequest(Request request);
}
