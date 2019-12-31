package com.softwareverde.http.server.servlet.request;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.cookie.CookieParser;
import com.softwareverde.http.form.MultiPartFormData;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.http.querystring.QueryStringParser;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.IoUtil;
import com.softwareverde.util.StringUtil;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RequestInflater {
    protected static Boolean headerContainsValue(final List<String> headerValues, final String value) {
        final String lowerCaseValue = value.toLowerCase();
        for (final String headerValue : headerValues) {
            final String lowerCaseHeaderValue = headerValue.toLowerCase();
            if (lowerCaseHeaderValue.contains(lowerCaseValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of a "; extraKey=value" extra within a header, or null if the key is not found.
     *  If the key is found, only the value is returned--excluding the extraKey and equal sign.
     */
    protected static String getHeaderExtra(final List<String> headerValues, final String extraKey) {
        final String lowerCaseExtraKey = extraKey.toLowerCase();
        for (final String headerValue : headerValues) {
            for (final String headerExtraValue : headerValue.split(";")) {
                final String trimmedExtraValue = StringUtil.pregMatch("^[\\s]*(.*)$", headerExtraValue).get(0);
                final String lowerCaseHeaderValue = trimmedExtraValue.toLowerCase();
                if (lowerCaseHeaderValue.startsWith(lowerCaseExtraKey + "=")) {
                    return trimmedExtraValue.substring(extraKey.length() + 1);
                }
            }
        }
        return null;
    }

    public static final QueryStringParser<GetParameters> GET_PARAMETERS_PARSER = new QueryStringParser<GetParameters>(new QueryStringParser.QueryStringFactory<GetParameters>() {
        @Override
        public GetParameters newInstance() {
            return new GetParameters();
        }
    });

    public static final QueryStringParser<PostParameters> POST_PARAMETERS_PARSER = new QueryStringParser<PostParameters>(new QueryStringParser.QueryStringFactory<PostParameters>() {
        @Override
        public PostParameters newInstance() {
            return new PostParameters();
        }
    });

    protected void _buildRequestBody(final Request request, final HttpExchange httpExchange) {
        try {
            final byte[] postBytes = IoUtil.readStream(httpExchange.getRequestBody());

            request._rawPostData = postBytes;

            boolean isTraditionalPost = true;
            final Headers headers = request.getHeaders();
            if (headers.containsHeader("content-type")) {
                final List<String> contentTypeHeaderValues = headers.getHeader("content-type");
                if (RequestInflater.headerContainsValue(contentTypeHeaderValues, "multipart/form-data")) {
                    isTraditionalPost = false;

                    final String boundary = RequestInflater.getHeaderExtra(contentTypeHeaderValues, "boundary");
                    if ( (boundary != null) && (! boundary.isEmpty()) ) {
                        request._multiPartFormData = MultiPartFormData.parseFromRequest(boundary, postBytes);
                    }
                }
            }

            if (isTraditionalPost) {
                request._postParameters = POST_PARAMETERS_PARSER.parse(StringUtil.bytesToString(postBytes));
            }
        }
        catch (final Exception exception) {
            Logger.warn(RequestInflater.class, "Unable to parse POST parameters.");
            Logger.warn(exception);
        }
    }

    protected void _buildCoreRequest(final Request request, final HttpExchange httpExchange) {
        final URI requestUri = httpExchange.getRequestURI();
        final String filePath = requestUri.getPath();

        request._hostname = new Request.HostNameLookup() {
            final InetSocketAddress _inetSocketAddress = httpExchange.getLocalAddress();

            @Override
            public String resolveHostName() {
                return _inetSocketAddress.getHostName();
            }

            @Override
            public String getHostName() {
                return _inetSocketAddress.toString();
            }
        };

        request._filePath = filePath;
        request._method = HttpMethod.fromString(httpExchange.getRequestMethod());

        { // Headers
            final com.sun.net.httpserver.Headers httpExchangeHeaders = httpExchange.getRequestHeaders();

            for (final String headerKey : httpExchangeHeaders.keySet()) {
                if (! request._headers.containsHeader(headerKey)) {
                    request._headers.setHeader(headerKey, new ArrayList<String>());
                }

                final List<String> headerValues = request._headers.getHeader(headerKey);
                headerValues.addAll(httpExchangeHeaders.get(headerKey));
                request._headers.setHeader(headerKey, headerValues);
            }
        }

        { // Cookies
            final CookieParser cookieParser = new CookieParser();
            for (final String headerKey : request._headers.getHeaderNames()) {
                if (! headerKey.equalsIgnoreCase("cookie")) { continue; }

                final List<String> cookieHeaderValues = request._headers.getHeader(headerKey);
                for (final String cookieHeaderValue : cookieHeaderValues) {
                    final List<Cookie> parsedCookies = cookieParser.parseFromCookieHeader(cookieHeaderValue);
                    request._cookies.addAll(parsedCookies);
                }
                break;
            }
        }

        try {
            final String rawQueryString = httpExchange.getRequestURI().getRawQuery();
            request._getParameters = GET_PARAMETERS_PARSER.parse(rawQueryString);
            request._rawQueryString = rawQueryString;
        }
        catch (final Exception exception) {
            Logger.warn(RequestInflater.class, "Unable to parse GET parameters.", exception);
        }
    }

    public GetParameters parseGetParameters(final String queryString) {
        return GET_PARAMETERS_PARSER.parse(queryString);
    }

    public PostParameters parsePostParameters(final String postBody) {
        return POST_PARAMETERS_PARSER.parse(postBody);
    }

    public Request createRequest(final HttpExchange httpExchange) {
        final Request request = new Request();
        _buildCoreRequest(request, httpExchange);
        _buildRequestBody(request, httpExchange);
        return request;
    }

    public WebSocketRequest createWebSocketRequest(final HttpExchange httpExchange) {
        final WebSocketRequest webSocketRequest = new WebSocketRequest();
        _buildCoreRequest(webSocketRequest, httpExchange);
        if (! webSocketRequest.isWebSocketRequest()) { // Consume the InputStream if the request is not intended to be a WebSocket...
            _buildRequestBody(webSocketRequest, httpExchange);
        }
        return webSocketRequest;
    }
}
