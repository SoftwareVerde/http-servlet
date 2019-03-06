package com.softwareverde.http.server.tomcat.request;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.util.Util;

import java.util.List;
import java.util.Map;

public class TomcatRequest extends Request {
    public void setHostname(final String hostname) {
        _hostname = new HostNameLookup() {
            @Override
            public String resolveHostName() {
                return hostname;
            }

            @Override
            public String getHostName() {
                return hostname;
            }
        };
    }

    public void setFilePath(final String filePath) {
        _filePath = filePath;
    }

    public void setMethod(final HttpMethod method) {
        _method = method;
    }

    public void setHeaders(final Map<String, List<String>> headers) {
        _headers.clear();
        for (final String key : headers.keySet()) {
            final List<String> values = headers.get(key);
            _headers.setHeader(key, Util.copyList(values));
        }
    }

    public void setCookies(final List<Cookie> cookies) {
        _cookies.clear();
        _cookies.addAll(cookies);
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
