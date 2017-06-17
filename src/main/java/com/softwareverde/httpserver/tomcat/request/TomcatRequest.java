package com.softwareverde.httpserver.tomcat.request;

import com.softwareverde.servlet.GetParameters;
import com.softwareverde.servlet.PostParameters;
import com.softwareverde.servlet.request.Request;
import com.softwareverde.util.Util;

import java.util.List;
import java.util.Map;

public class TomcatRequest extends Request {
    public void setHostname(final String hostname) {
        _hostname = hostname;
    }

    public void setFilePath(final String filePath) {
        _filePath = filePath;
    }

    public void setMethod(final HttpMethod method) {
        _method = method;
    }

    public void setHeaders(final Map<String, List<String>> headers) {
        for (final String key : headers.keySet()) {
            final List<String> values = headers.get(key);
            _headers.put(key, Util.copyList(values));
        }
    }

    public void setGetParameters(final GetParameters getParameters) {
        _getParameters = getParameters;
    }

    public void setRawQueryString(final String queryString) {
        _rawQueryString = queryString;
    }

    public void setPostParameters(final PostParameters postParameters) {
        _postParameters = postParameters;
    }

    public void setRawPostData(final byte[] rawPostData) {
        _rawPostData = rawPostData;
    }
}
