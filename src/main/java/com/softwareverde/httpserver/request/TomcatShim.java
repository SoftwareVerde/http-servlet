package com.softwareverde.httpserver.request;

import com.softwareverde.httpserver.HttpServer;
import com.softwareverde.httpserver.response.Response;
import com.softwareverde.util.IoUtil;
import com.softwareverde.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TomcatShim extends HttpServlet {
    public static Request createRequestFromTomcatRequest(final HttpServletRequest httpServletRequest) {
        final Request request = new Request();

        request._hostname = httpServletRequest.getLocalAddr();
        request._filePath = httpServletRequest.getRequestURL().toString();
        request._method = Request.HttpMethod.fromString(httpServletRequest.getMethod());

        request._headers = new HashMap<String, List<String>>();
        {
            final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    final List<String> headers = new ArrayList<String>();

                    final String headerName = headerNames.nextElement();
                    final Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
                    if (headerValues != null) {
                        while (headerValues.hasMoreElements()) {
                            final String headerValue = headerValues.nextElement();
                            headers.add(headerValue);
                        }
                    }

                    request._headers.put(headerName, headers);
                }
            }
        }

        try {
            final String rawQueryString = httpServletRequest.getQueryString();
            request._getParameters = Request._getParametersParser.parse(rawQueryString);
            request._rawQueryString = rawQueryString;
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        try {
            final String rawPostData = IoUtil.streamToString(httpServletRequest.getInputStream());
            request._postParameters = Request._postParametersParser.parse(rawPostData);
            request._rawPostData = StringUtil.stringToBytes(rawPostData);
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        return request;
    }

    protected final HttpServer.RequestHandler _requestHandler;

    protected void _handleRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String responseContent = "Server error.";
        try {
            final Response response = _requestHandler.onRequest(TomcatShim.createRequestFromTomcatRequest(httpServletRequest));

            httpServletResponse.setStatus(response.getCode());

            { // Send Response Headers
                final Map<String, List<String>> compiledHeaders = response.getHeaders();
                for (final String headerKey : compiledHeaders.keySet()) {
                    final List<String> headerValues = compiledHeaders.get(headerKey);
                    for (final String headerValue : headerValues) {
                        httpServletResponse.addHeader(headerKey, headerValue);
                    }
                }
            }

            final byte[] responseBytes = response.getContent();
            httpServletResponse.setContentLength(responseBytes.length);
            responseContent = StringUtil.bytesToString(responseBytes);
        }
        catch (final Exception exception) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        final PrintWriter writer = httpServletResponse.getWriter();
        writer.write(responseContent);
    }

    public TomcatShim(final HttpServer.RequestHandler requestHandler) {
        _requestHandler = requestHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _handleRequest(req, resp);
    }
}
