package com.softwareverde.servlet;

import com.softwareverde.servlet.request.Request;
import com.softwareverde.servlet.response.Response;

public interface Servlet {
    Response onRequest(Request request);
}
