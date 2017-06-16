package com.softwareverde.httpserver.tomcat;

import com.softwareverde.httpserver.HttpServer;
import com.softwareverde.httpserver.tomcat.request.TomcatRequest;
import com.softwareverde.servlet.response.Response;
import com.softwareverde.servlet.request.Request;
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
        final TomcatRequest request = new TomcatRequest();

        request.setHostname(httpServletRequest.getLocalAddr());
        request.setFilePath(httpServletRequest.getRequestURL().toString());
        request.setMethod(Request.HttpMethod.fromString(httpServletRequest.getMethod()));

        final Map<String, List<String>> _headers = new HashMap<String, List<String>>();
        {
            final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    final List<String> headerNameValues = new ArrayList<String>();

                    final String headerName = headerNames.nextElement();
                    final Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
                    if (headerValues != null) {
                        while (headerValues.hasMoreElements()) {
                            final String headerValue = headerValues.nextElement();
                            headerNameValues.add(headerValue);
                        }
                    }

                    _headers.put(headerName, headerNameValues);
                }
            }
        }
        request.setHeaders(_headers);

        try {
            final String rawQueryString = httpServletRequest.getQueryString();
            request.setGetParameters(Request.parseGetParameters(rawQueryString));
            request.setRawQueryString(rawQueryString);
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        try {
            final String rawPostData = IoUtil.streamToString(httpServletRequest.getInputStream());
            request.setPostParameters(Request.parsePostParameters(rawPostData));
            request.setRawPostData(StringUtil.stringToBytes(rawPostData));
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
