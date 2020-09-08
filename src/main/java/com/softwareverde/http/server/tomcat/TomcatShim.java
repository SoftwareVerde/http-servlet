package com.softwareverde.http.server.tomcat;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.server.servlet.Servlet;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.request.RequestInflater;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.tomcat.request.TomcatRequest;
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

        request.setLocalHost(httpServletRequest);
        request.setRemoteHost(httpServletRequest);
        request.setFilePath(httpServletRequest.getRequestURL().toString());
        request.setMethod(HttpMethod.fromString(httpServletRequest.getMethod()));

        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
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

                    headers.put(headerName, headerNameValues);
                }
            }
        }
        request.setHeaders(headers);

        final List<Cookie> cookies = new ArrayList<Cookie>();
        {
            for (final javax.servlet.http.Cookie tomcatCookie : httpServletRequest.getCookies()) {
                final Cookie cookie = new Cookie();

                cookie.setKey(tomcatCookie.getName());
                cookie.setValue(tomcatCookie.getValue());
                cookie.setPath(tomcatCookie.getPath());
                cookie.setDomain(tomcatCookie.getDomain());
                cookie.setMaxAge(tomcatCookie.getMaxAge(), true);
                cookie.setIsHttpOnly(tomcatCookie.isHttpOnly());
                cookie.setIsSecure(tomcatCookie.getSecure());
                cookie.setIsSameSiteStrict(false);

                cookies.add(cookie);
            }
        }
        request.setCookies(cookies);

        try {
            final String rawQueryString = httpServletRequest.getQueryString();
            request.setGetParameters(RequestInflater.GET_PARAMETERS_PARSER.parse(rawQueryString));
            request.setRawQueryString(rawQueryString);
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        try {
            final String rawPostData = IoUtil.streamToString(httpServletRequest.getInputStream());
            request.setPostParameters(RequestInflater.POST_PARAMETERS_PARSER.parse(rawPostData));
            request.setRawPostData(StringUtil.stringToBytes(rawPostData));
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        return request;
    }

    protected final Servlet _servlet;

    protected void _handleRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String responseContent = "Server error.";
        try {
            final Response response = _servlet.onRequest(TomcatShim.createRequestFromTomcatRequest(httpServletRequest));

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

    public TomcatShim(final Servlet servlet) {
        _servlet = servlet;
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
