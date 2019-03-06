package com.softwareverde.http.server.servlet.request;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Request {
    protected interface HostNameLookup {
        String resolveHostName();
        String getHostName();
    }

    protected HostNameLookup _hostname; // The hostname of the server. (e.x.: "softwareverde.com")
    protected String _filePath;         // The filepath of the request-url. (e.x.: "/index.html")
    protected HttpMethod _method;

    protected final Headers _headers = new Headers();
    protected final List<Cookie> _cookies = new ArrayList<Cookie>();

    protected GetParameters _getParameters;
    protected PostParameters _postParameters;

    protected String _rawQueryString; // (e.x. "?key=value")
    protected byte[] _rawPostData;

    public String resolveHostname() { return _hostname.resolveHostName(); }
    public String getHostname() { return _hostname.getHostName(); }

    public String getFilePath() { return _filePath; }
    public HttpMethod getMethod() { return _method; }

    public GetParameters getGetParameters() { return _getParameters; }
    public PostParameters getPostParameters() { return _postParameters; }

    public Headers getHeaders() {
        return new Headers(_headers);
    }

    public List<Cookie> getCookies() { return Util.copyList(_cookies); }

    public byte[] getRawPostData() { return _rawPostData; }
    public String getQueryString() { return _rawQueryString; }
}
