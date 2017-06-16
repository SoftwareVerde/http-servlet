package com.softwareverde.httpserver;

import com.softwareverde.httpserver.request.Request;
import com.softwareverde.httpserver.response.JsonResponse;
import com.softwareverde.httpserver.response.JsonResult;
import com.softwareverde.httpserver.response.Response;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

class HttpHandler implements com.sun.net.httpserver.HttpHandler {
    private HttpServer.RequestHandler _requestHandler;

    private Boolean _isPathStrictlyMatched(final HttpExchange httpExchange) {
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

    public HttpHandler(final HttpServer.RequestHandler requestHandler) {
        _requestHandler = requestHandler;
    }

    public HttpServer.RequestHandler getRequestHandler() {
        return _requestHandler;
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final Boolean pathIsStrictMatch = _isPathStrictlyMatched(httpExchange);

        final Response response;
        {
            if ( (_requestHandler.isStrictPathEnabled()) && (! pathIsStrictMatch) ) {
                response = new JsonResponse(Response.ResponseCodes.NOT_FOUND, new JsonResult(false, "Not found."));
            }
            else {
                final Request request = Request.createRequest(httpExchange);
                if (request == null) {
                    response = new JsonResponse(Response.ResponseCodes.SERVER_ERROR, new JsonResult(false, "Bad request."));
                }
                else {
                    Response requestHandlerResponse;
                    {
                        try {
                            requestHandlerResponse = _requestHandler.onRequest(request);
                        }
                        catch (final Exception exception) {
                            System.err.println("\n-- Error handling request: " + httpExchange.getRequestURI());
                            exception.printStackTrace();
                            System.err.println("--\n");
                            requestHandlerResponse = new JsonResponse(Response.ResponseCodes.SERVER_ERROR, new JsonResult(false, "Server error."));
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
        }

        final byte[] responseBytes = response.getContent();
        httpExchange.sendResponseHeaders(response.getCode(), responseBytes.length);

        final OutputStream os = httpExchange.getResponseBody();
        os.write(responseBytes);
        os.flush();
        os.close();

        httpExchange.close();
    }
}