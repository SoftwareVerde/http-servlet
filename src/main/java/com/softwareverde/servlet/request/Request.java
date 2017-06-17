package com.softwareverde.servlet.request;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.cookie.CookieParser;
import com.softwareverde.servlet.GetParameters;
import com.softwareverde.servlet.PostParameters;
import com.softwareverde.servlet.querystring.QueryStringParser;
import com.softwareverde.util.IoUtil;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.Util;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    public enum HttpMethod {
        GET, POST, HEAD, PUT, DELETE, OPTIONS, TRACE;

        public static HttpMethod fromString(final String string) {
            for (final HttpMethod httpMethod : HttpMethod.values()) {
                if (httpMethod.name().equalsIgnoreCase(string)) {
                    return httpMethod;
                }
            }

            return null;
        }
    }

    protected static final QueryStringParser<GetParameters> _getParametersParser = new QueryStringParser<GetParameters>(new QueryStringParser.QueryStringFactory<GetParameters>() {
        @Override
        public GetParameters newInstance() {
            return new GetParameters();
        }
    });

    protected static final QueryStringParser<PostParameters> _postParametersParser = new QueryStringParser<PostParameters>(new QueryStringParser.QueryStringFactory<PostParameters>() {
        @Override
        public PostParameters newInstance() {
            return new PostParameters();
        }
    });

    public static GetParameters parseGetParameters(final String queryString) {
        return _getParametersParser.parse(queryString);
    }

    public static PostParameters parsePostParameters(final String postBody) {
        return _postParametersParser.parse(postBody);
    }

    protected String _hostname;     // The hostname of the server. (e.x.: "softwareverde.com")
    protected String _filePath;     // The filepath of the request-url. (e.x.: "/index.html")
    protected HttpMethod _method;

    protected final Map<String, List<String>> _headers = new HashMap<String, List<String>>();
    protected final List<Cookie> _cookies = new ArrayList<Cookie>();

    protected GetParameters _getParameters;
    protected PostParameters _postParameters;

    protected String _rawQueryString; // (e.x. "?key=value")
    protected byte[] _rawPostData;

    public static Request createRequest(final HttpExchange httpExchange) {
        final URI requestUri = httpExchange.getRequestURI();
        final String host = httpExchange.getLocalAddress().getHostName();
        final String filePath = requestUri.getPath();

        final Request request = new Request();
        request._hostname = host;
        request._filePath = filePath;
        request._method = HttpMethod.fromString(httpExchange.getRequestMethod());

        { // Headers
            final Headers httpExchangeHeaders = httpExchange.getRequestHeaders();

            for (final String headerKey : httpExchangeHeaders.keySet()) {
                if (! request._headers.containsKey(headerKey)) {
                    request._headers.put(headerKey, new ArrayList<String>());
                }

                final List<String> headerValues = request._headers.get(headerKey);
                headerValues.addAll(httpExchangeHeaders.get(headerKey));
            }
        }

        { // Cookies
            final CookieParser cookieParser = new CookieParser();
            for (final String headerKey : request._headers.keySet()) {
                if (! headerKey.equalsIgnoreCase("cookie")) { continue; }

                final List<String> cookieHeaderValues = request._headers.get(headerKey);
                for (final String cookieHeaderValue : cookieHeaderValues) {
                    final List<Cookie> parsedCookies = cookieParser.parseFromCookieHeader(cookieHeaderValue);
                    request._cookies.addAll(parsedCookies);
                }
                break;
            }
        }

        try {
            final String rawQueryString = httpExchange.getRequestURI().getRawQuery();
            request._getParameters = _getParametersParser.parse(rawQueryString);
            request._rawQueryString = rawQueryString;
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        try {
            final byte[] postBytes = IoUtil.readStream(httpExchange.getRequestBody());
            request._postParameters = _postParametersParser.parse(StringUtil.bytesToString(postBytes));
            request._rawPostData = postBytes;
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        return request;
    }

    public String getHostname() { return _hostname; }
    public String getFilePath() { return _filePath; }
    public HttpMethod getMethod() { return _method; }

    public GetParameters getGetParameters() { return _getParameters; }
    public PostParameters getPostParameters() { return _postParameters; }

    public Map<String, List<String>> getHeaders() {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (final String key : _headers.keySet()) {
            final List<String> values = _headers.get(key);
            headers.put(key, Util.copyList(values));
        }
        return headers;
    }

    public List<Cookie> getCookies() { return Util.copyList(_cookies); }

    public byte[] getRawPostData() { return _rawPostData; }
    public String getQueryString() { return _rawQueryString; }
}
