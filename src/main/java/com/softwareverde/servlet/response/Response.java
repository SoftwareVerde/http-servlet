package com.softwareverde.servlet.response;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
    public static class ResponseCodes {
        public static final Integer OK = 200;
        public static final Integer MOVED_PERMANENTLY = 301;
        public static final Integer MOVED_TEMPORARILY = 302;
        public static final Integer MOVED = 302;
        public static final Integer BAD_REQUEST = 400;
        public static final Integer NOT_AUTHORIZED = 401;
        public static final Integer NOT_FOUND = 404;
        public static final Integer SERVER_ERROR = 500;
    }
    public static class Headers {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String SET_COOKIE = "Set-Cookie";
    }

    protected final Map<String, List<String>> _headers = new HashMap<String, List<String>>();
    protected final List<Cookie> _cookies = new ArrayList<Cookie>();

    protected Integer _code = ResponseCodes.OK;
    protected byte[] _content = (new byte[0]);

    public Response() { }

    public void setContent(final byte[] content) {
        _content = content;
    }

    public void setContent(final String content) {
        try {
            _content = content.getBytes("UTF-8");
        }
        catch (final Exception e) { }
    }

    public void setCode(final Integer code) { _code = code; }

    public Integer getCode() { return _code; }
    public byte[] getContent() { return _content; }

    public void clearHeaders() {
        _headers.clear();
    }

    public void clearHeader(final String key) {
        if (! _headers.containsKey(key)) { return; }

        _headers.get(key).clear();
    }

    public void addHeader(final String key, final String value) {
        if (! _headers.containsKey(key)) {
            _headers.put(key, new ArrayList<String>());
        }

        _headers.get(key).add(value);
    }

    public Map<String, List<String>> getHeaders() { return _headers; }

    public void clearCookies() {
        _cookies.clear();
    }

    public void addCookie(final Cookie cookie) {
        _cookies.add(cookie);
    }

    public List<Cookie> getCookies() {
        return Util.copyList(_cookies);
    }
}
