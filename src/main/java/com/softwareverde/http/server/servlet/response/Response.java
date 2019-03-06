package com.softwareverde.http.server.servlet.response;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.util.Base64Util;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.Util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
    public static class Codes {
        protected Codes() { }

        public static final Integer SWITCHING_PROTOCOLS = 101;
        public static final Integer OK = 200;
        public static final Integer MOVED_PERMANENTLY = 301;
        public static final Integer MOVED_TEMPORARILY = 302;
        public static final Integer BAD_REQUEST = 400;
        public static final Integer NOT_AUTHORIZED = 401;
        public static final Integer NOT_FOUND = 404;
        public static final Integer SERVER_ERROR = 500;
    }

    public static class Headers {
        protected Headers() { }

        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String SET_COOKIE = "Set-Cookie";
        public static final String COOKIE = "Cookie";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONNECTION = "Connection";
        public static final String UPGRADE = "Upgrade";

        public static class WebSocket {
            private WebSocket() { }

            public static final String KEY = "Sec-WebSocket-Key";
            public static final String ACCEPT = "Sec-WebSocket-Accept";

            public static class Values {
                private Values() { }

                protected static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

                public static final String CONNECTION = "upgrade";
                public static final String UPGRADE = "websocket";

                public static String createAcceptHeader(final String webSocketKey) {
                    try {
                        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
                        final byte[] payload = messageDigest.digest(StringUtil.stringToBytes(webSocketKey + GUID));
                        return Base64Util.toBase64String(payload);
                    }
                    catch (final Exception exception) { }
                    return null;
                }
            }
        }
    }

    protected final Map<String, List<String>> _headers = new HashMap<String, List<String>>();
    protected final List<Cookie> _cookies = new ArrayList<Cookie>();

    protected Integer _code = Codes.OK;
    protected byte[] _content = (new byte[0]);

    public Response() { }

    public void setContent(final byte[] content) {
        _content = content;
    }

    public void setContent(final String content) {
        try {
            _content = (content != null ? StringUtil.stringToBytes(content) : null);
        }
        catch (final Exception exception) { }
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

    public void setHeader(final String key, final String value) {
        final List<String> values = new ArrayList<String>();
        values.add(value);

        _headers.put(key, values);
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
