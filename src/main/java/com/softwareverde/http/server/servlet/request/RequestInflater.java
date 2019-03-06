package com.softwareverde.http.server.servlet.request;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.cookie.CookieParser;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.http.querystring.QueryStringParser;
import com.softwareverde.logging.Log;
import com.softwareverde.util.IoUtil;
import com.softwareverde.util.StringUtil;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RequestInflater {
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
            request._postParameters = POST_PARAMETERS_PARSER.parse(StringUtil.bytesToString(postBytes));
            request._rawPostData = postBytes;
        }
        catch (final Exception exception) {
            Log.error("Unable to parse POST parameters.");
        }
    }

    protected Request _buildCoreRequest(final Request request, final HttpExchange httpExchange) {
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
            Log.error("Unable to parse GET parameters.", exception);
        }

        return request;
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
        return webSocketRequest;
    }
}
