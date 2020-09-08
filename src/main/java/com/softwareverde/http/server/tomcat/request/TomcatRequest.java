package com.softwareverde.http.server.tomcat.request;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.http.server.servlet.request.HostInformation;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.util.Util;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class TomcatRequest extends Request {
    public void setRemoteHost(final HttpServletRequest request) {
        _remoteHost = new HostInformation() {
            final
            @Override
            public String resolveHostName() {
                return request.getRemoteHost();
            }

            @Override
            public boolean isHostNameResolved() {
                return (! Util.isBlank(request.getRemoteHost()));
            }

            @Override
            public String getHostInfo() {
                return request.getRemoteAddr() + ":" + request.getRemotePort();
            }

            @Override
            public String getIpAddress() {
                return request.getRemoteAddr();
            }

            @Override
            public int getPort() {
                return request.getRemotePort();
            }
        };
    }

    public void setLocalHost(final HttpServletRequest request) {
        _localHost = new HostInformation() {
            @Override
            public String resolveHostName() {
                return request.getLocalName();
            }

            @Override
            public boolean isHostNameResolved() {
                return (! Util.isBlank(request.getLocalName()));
            }

            @Override
            public String getHostInfo() {
                return request.getLocalAddr() + ":" + request.getLocalPort();
            }

            @Override
            public String getIpAddress() {
                return request.getLocalAddr();
            }

            public int getPort() {
                return request.getLocalPort();
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
