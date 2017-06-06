package com.softwareverde.httpserver.request;

import com.softwareverde.httpserver.GetParameters;
import com.softwareverde.httpserver.PostParameters;
import com.softwareverde.httpserver.QueryStringParser;
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
    protected String _host;
    protected String _url;
    protected String _filePath;

    protected Map<String, List<String>> _headers;
    protected GetParameters _getParameters;
    protected PostParameters _postParameters;

    protected byte[] _rawPostData;

    private static final QueryStringParser<GetParameters> _getParametersParser = new QueryStringParser<GetParameters>(new QueryStringParser.QueryStringFactory<GetParameters>() {
        @Override
        public GetParameters newInstance() {
            return new GetParameters();
        }
    });

    private static final QueryStringParser<PostParameters> _postParametersParser = new QueryStringParser<PostParameters>(new QueryStringParser.QueryStringFactory<PostParameters>() {
        @Override
        public PostParameters newInstance() {
            return new PostParameters();
        }
    });

    public static Request createRequest(final HttpExchange httpExchange) {
        final URI requestUri = httpExchange.getRequestURI();
        final String host = httpExchange.getLocalAddress().getHostName();
        final String url = requestUri.toASCIIString();
        final String filePath = requestUri.getPath();

        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        final Headers httpExchangeHeaders = httpExchange.getRequestHeaders();

        for (final String headerKey : httpExchangeHeaders.keySet()) {
            if (! headers.containsKey(headerKey)) {
                headers.put(headerKey, new ArrayList<String>());
            }

            final List<String> headerValues = headers.get(headerKey);
            headerValues.addAll(httpExchangeHeaders.get(headerKey));
        }

        final Request request = new Request();
        request._host = host;
        request._url = url;
        request._filePath = filePath;

        request._headers = headers;

        try {
            final byte[] postBytes = IoUtil.readStream(httpExchange.getRequestBody());
            request._postParameters = _postParametersParser.parse(StringUtil.bytesToString(postBytes));
            request._rawPostData = postBytes;
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        try {
            request._getParameters = _getParametersParser.parse(httpExchange.getRequestURI().getRawQuery());
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        return request;
    }

    public String getHost() { return _host; }
    public String getUrl() { return _url; }
    public String getFilePath() { return _filePath; }

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

    public byte[] getRawPostData() { return _rawPostData; }
}
