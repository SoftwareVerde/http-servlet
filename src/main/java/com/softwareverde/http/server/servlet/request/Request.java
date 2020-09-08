package com.softwareverde.http.server.servlet.request;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.form.MultiPartFormData;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Request {
    /**
     * Returns true if the header is a WebSocket initialization header (aka: "Upgrade WebSocket").
     */
    public static Boolean isWebSocketHeader(final String headerKey, final String headerValue) {
        return (Util.areEqual("upgrade", Util.coalesce(headerKey).toLowerCase()) && Util.areEqual("websocket", Util.coalesce(headerValue).toLowerCase()));
    }

    protected HostInformation _remoteHost; // the client connecting to the server
    protected HostInformation _localHost; // the server itself (e.x.: "softwareverde.com")
    protected String _filePath;         // The filepath of the request-url. (e.x.: "/index.html")
    protected HttpMethod _method;

    protected final Headers _headers = new Headers();
    protected final List<Cookie> _cookies = new ArrayList<Cookie>();

    protected GetParameters _getParameters;
    protected PostParameters _postParameters;
    protected MultiPartFormData _multiPartFormData;

    protected String _rawQueryString; // (e.x. "?key=value")
    protected byte[] _rawPostData;

    public HostInformation getRemoteHostInformation() { return _remoteHost; }
    public HostInformation getLocalHostInformation() { return _localHost; }

    @Deprecated
    public String resolveHostname() { return _localHost.resolveHostName(); }
    @Deprecated
    public String getHostname() { return _localHost.getHostInfo(); }

    public String getFilePath() { return _filePath; }
    public HttpMethod getMethod() { return _method; }

    public GetParameters getGetParameters() { return _getParameters; }
    public PostParameters getPostParameters() { return _postParameters; }

    public Headers getHeaders() {
        return new Headers(_headers);
    }

    public List<Cookie> getCookies() { return Util.copyList(_cookies); }
    public Cookie getCookie(final String cookieName) {
        for (final Cookie cookie : _cookies) {
            final String cookieKey = cookie.getKey();
            if (Util.areEqual(cookieName.toLowerCase(), cookieKey.toLowerCase())) {
                return cookie;
            }
        }
        return null;
    }

    public byte[] getRawPostData() { return _rawPostData; }
    public String getQueryString() { return _rawQueryString; }
    public MultiPartFormData getMultiPartFormData() { return _multiPartFormData; }
}
