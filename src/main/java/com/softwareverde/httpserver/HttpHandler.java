package com.softwareverde.httpserver;

import com.softwareverde.httpserver.response.JsonResponse;
import com.softwareverde.httpserver.response.JsonResult;
import com.softwareverde.util.Util;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

class HttpHandler<T> implements com.sun.net.httpserver.HttpHandler {
    private ApiServer.RequestHandler<T> _requestHandler;

    private QueryStringParser<GetParameters> _getParametersParser = new QueryStringParser<GetParameters>(new QueryStringParser.QueryStringFactory<GetParameters>() {
        @Override
        public GetParameters newInstance() {
            return new GetParameters();
        }
    });

    private QueryStringParser<PostParameters> _postParametersParser = new QueryStringParser<PostParameters>(new QueryStringParser.QueryStringFactory<PostParameters>() {
        @Override
        public PostParameters newInstance() {
            return new PostParameters();
        }
    });

    private GetParameters _parseGetParameters(final HttpExchange exchange) {
        try {
            return _getParametersParser.parse(exchange.getRequestURI().getRawQuery());
        } catch (final Exception e) { e.printStackTrace(); }
        return null;
    }

    private PostParameters _parsePostParameters(final HttpExchange exchange) {
        try {
            return _postParametersParser.parse(Util.streamToString(exchange.getRequestBody()));
        } catch (final Exception e) { e.printStackTrace(); }
        return null;
    }

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

    public HttpHandler(final ApiServer.RequestHandler<T> requestHandler) {
        _requestHandler = requestHandler;
    }

    public ApiServer.RequestHandler<T> getRequestHandler() {
        return _requestHandler;
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final Boolean pathIsStrictMatch = _isPathStrictlyMatched(httpExchange);

        ApiServer.Response response;

        if ( (_requestHandler.isStrictPathEnabled()) && (! pathIsStrictMatch) ) {
            response = new JsonResponse(ApiServer.Response.ResponseCodes.NOT_FOUND, new JsonResult(false, "Not found."));
        }
        else {
            final GetParameters getParameters = _parseGetParameters(httpExchange);
            final PostParameters postParameters = _parsePostParameters(httpExchange);

            if (getParameters == null || postParameters == null) {
                response = new JsonResponse(ApiServer.Response.ResponseCodes.SERVER_ERROR, new JsonResult(false, "Bad request."));
            }
            else {
                try {
                    if (_requestHandler instanceof ApiServer.Endpoint) {
                        response = ((ApiServer.Endpoint) _requestHandler).onRequest(getParameters, postParameters);
                    }
                    else if (_requestHandler instanceof ApiServer.StaticContent) {
                        final URI requestUri = httpExchange.getRequestURI();
                        final String host = httpExchange.getLocalAddress().getHostName();
                        final String uri = requestUri.toASCIIString();
                        final String filePath = requestUri.getPath();
                        response = ((ApiServer.StaticContent) _requestHandler).onRequest(host, uri, filePath);
                    }
                    else {
                        throw new RuntimeException("Unexpected RequestHandler Type: "+ _requestHandler.getClass().getSimpleName());
                    }
                } catch (final Exception e) {
                    System.err.println("\n-- Error handling request: "+ httpExchange.getRequestURI());
                    e.printStackTrace();
                    System.err.println("--\n");
                    response = new JsonResponse(ApiServer.Response.ResponseCodes.SERVER_ERROR, new JsonResult(false, "Server error."));
                }
            }
        }

        final byte[] responseBytes = response.getContent();

        final Headers httpExchangeHeaders = httpExchange.getResponseHeaders();
        final Map<String, List<String>> headers = response.getHeaders();
        for (final String headerKey : headers.keySet()) {
            final StringBuilder headerValueBuilder = new StringBuilder();
            final List<String> headerValues = headers.get(headerKey);
            for (Integer i = 0; i < headerValues.size(); ++i) {
                final String headerValue = headerValues.get(i);
                headerValueBuilder.append(headerValue);

                if (i < headerValues.size() - 1) {
                    headerValueBuilder.append("; ");
                }
            }

            httpExchangeHeaders.add(headerKey, headerValueBuilder.toString());
        }
        httpExchange.sendResponseHeaders(response.getCode(), responseBytes.length);

        final OutputStream os = httpExchange.getResponseBody();
        os.write(responseBytes);
        os.flush();
        os.close();

        httpExchange.close();
    }
}