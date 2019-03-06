package com.softwareverde.http.server;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.cookie.CookieParser;
import com.softwareverde.http.server.servlet.Servlet;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.request.RequestInflater;
import com.softwareverde.http.server.servlet.response.Response;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

class HttpHandler implements com.sun.net.httpserver.HttpHandler {
    protected final Servlet _servlet;
    protected final Boolean _shouldUseStrictPathMatching;

    protected static Boolean isPathStrictlyMatched(final HttpExchange httpExchange) {
        final String uriPath;
        {
            final String rawUriPath = httpExchange.getRequestURI().getPath();
            if ( (! rawUriPath.isEmpty()) && (rawUriPath.charAt(rawUriPath.length() - 1) == '/') ) {
                uriPath = rawUriPath.substring(0, rawUriPath.length() - 1);
            }
            else {
                uriPath = rawUriPath;
            }
        }
        final String definedPath;
        {
            final String rawDefinedPath = httpExchange.getHttpContext().getPath();
            if ( (! rawDefinedPath.isEmpty()) && (rawDefinedPath.charAt(rawDefinedPath.length() - 1) == '/') ) {
                definedPath = rawDefinedPath.substring(0, rawDefinedPath.length() - 1);
            }
            else {
                definedPath = rawDefinedPath;
            }
        }

        return uriPath.equals(definedPath);
    }

    public HttpHandler(final Servlet servlet, final Boolean shouldUseStrictPathMatching) {
        _servlet = servlet;
        _shouldUseStrictPathMatching = shouldUseStrictPathMatching;
    }

    public Servlet getServlet() {
        return _servlet;
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final Boolean pathIsStrictMatch = isPathStrictlyMatched(httpExchange);

        final Response response;
        {
            if ( (_shouldUseStrictPathMatching) && (! pathIsStrictMatch) ) {
                response = Util.createJsonErrorResponse(Response.Codes.NOT_FOUND, "Not found.");
            }
            else {
                final RequestInflater requestInflater = new RequestInflater();
                final Request request = requestInflater.createRequest(httpExchange);
                if (request == null) {
                    response = Util.createJsonErrorResponse(Response.Codes.SERVER_ERROR, "Bad request.");
                }
                else {
                    Response requestHandlerResponse;
                    {
                        try {
                            requestHandlerResponse = _servlet.onRequest(request);
                        }
                        catch (final Exception exception) {
                            System.err.println("\n-- Error handling request: " + httpExchange.getRequestURI());
                            exception.printStackTrace();
                            System.err.println("--\n");
                            requestHandlerResponse = Util.createJsonErrorResponse(Response.Codes.SERVER_ERROR, "Server error.");
                        }
                    }
                    response = requestHandlerResponse;
                }
            }
        }

        { // Send Response Headers
            final Headers httpExchangeHeaders = httpExchange.getResponseHeaders();
            final Map<String, List<String>> compiledHeaders = response.getHeaders();
            for (final String headerKey : compiledHeaders.keySet()) {
                final List<String> headerValues = compiledHeaders.get(headerKey);
                for (final String headerValue : headerValues) {
                    httpExchangeHeaders.add(headerKey, headerValue);
                }
            }

            final List<Cookie> cookies = response.getCookies();
            final CookieParser cookieParser = new CookieParser();
            final List<String> compiledCookies = cookieParser.compileCookiesIntoSetCookieHeaderValues(cookies);
            for (final String setCookieHeader : compiledCookies) {
                httpExchangeHeaders.add(Response.Headers.SET_COOKIE, setCookieHeader);
            }
        }

        final OutputStream outputStream = httpExchange.getResponseBody();

        final byte[] responseBytes = response.getContent();
        httpExchange.sendResponseHeaders(response.getCode(), (responseBytes == null ? -1 : responseBytes.length));

        if (responseBytes != null) {
            outputStream.write(responseBytes);
            outputStream.flush();
        }

        outputStream.close();
        httpExchange.close();
    }
}