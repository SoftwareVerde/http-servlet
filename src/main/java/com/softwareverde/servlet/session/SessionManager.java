package com.softwareverde.servlet.session;

import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.servlet.PostParameters;
import com.softwareverde.servlet.request.Request;
import com.softwareverde.security.AuthorizationKeyFactory;
import com.softwareverde.util.Util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager<T> {
    private final Map<String, T> _sessions = new ConcurrentHashMap<String, T>();
    private final AuthorizationKeyFactory _authorizationKeyFactory = new AuthorizationKeyFactory(256);
    protected String _authorizationTokenKey = "token";
    protected String _sessionHeaderKey = "Authorization";
    protected String _cookieKey = "SESSION_ID";

    /**
     * Sets the key used for the Session's API/POST token.
     *  The default value is "token".
     *  Setting this value to null will disable post parameter authorization tokens.
     */
    public void setPostParameterKey(final String authorizationTokenKey) {
        _authorizationTokenKey = authorizationTokenKey;
    }
    public String getPostParameterKey() {
        return _authorizationTokenKey;
    }

    /**
     * Sets the key used for the Session's authorization header.
     *  The default value is "Authorization".
     *  Setting this value to null will disable header authentication.
     */
    public void setHeaderKey(final String sessionHeaderKey) {
        _sessionHeaderKey = sessionHeaderKey;
    }
    public String getHeaderKey() {
        return _sessionHeaderKey;
    }

    /**
     * Sets the name used for the Session's cookie.
     *  The default value is "SESSION_ID".
     *  If set to null, authorization via SessionCookies will be disabled.
     */
    public void setCookieKey(final String cookieKey) {
        _cookieKey = cookieKey;
    }
    public String getCookieKey() {
        return _cookieKey;
    }

    public String generateNewAuthorizationToken() {
        return _authorizationKeyFactory.generateAuthorizationKey();
    }

    public Cookie generateSessionCookie(final String authorizationToken) {
        final Cookie sessionCookie = new Cookie();

        sessionCookie.setKey(_sessionHeaderKey);
        sessionCookie.setValue(authorizationToken);
        sessionCookie.setIsHttpOnly(true);
        sessionCookie.setIsSecure(true);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(365 * 24 * 60 * 60, true); // 1 Year

        return sessionCookie;
    }

    /**
     * Returns T set by setAuthorizedSession() that has authorized by either the auth cookie or post parameters.
     *  Returns null if the session is not authorized.
     *  The hierarchy used is:
     *      Session Cookie > Authorization Header > POST Token
     */
    public T getSession(final Request request) {
        final PostParameters postParameters = request.getPostParameters();

        final String authorizationToken;
        {
            // Check via Session Cookie...
            if (_cookieKey != null) {
                String authorizationCookieValue = "";
                final List<Cookie> cookies = request.getCookies();
                for (final Cookie cookie : cookies) {
                    if (_cookieKey.equalsIgnoreCase(cookie.getKey())) {
                        authorizationCookieValue = cookie.getValue();
                        break;
                    }
                }
                authorizationToken = authorizationCookieValue;
            }

            // Check via Authorization Header...
            else if (_sessionHeaderKey != null) {
                String authorizationHeaderValue = "";
                final Map<String, List<String>> headers = request.getHeaders();
                for (final String headerName : headers.keySet()) {
                    if (! _sessionHeaderKey.equalsIgnoreCase(headerName)) { continue; }

                    final List<String> sessionCookies = headers.get(headerName);
                    authorizationHeaderValue = (sessionCookies.isEmpty() ? "" : sessionCookies.get(0).trim());
                    break;
                }
                authorizationToken = authorizationHeaderValue;
            }

            // Check via POST Token...
            else if (_authorizationTokenKey != null) {
                authorizationToken = Util.coalesce(postParameters.get(_authorizationTokenKey)).trim();
            }

            // No match found.
            else {
                authorizationToken = "";
            }
        }

        if (authorizationToken.length() == 0) { return null; }

        return _sessions.get(authorizationToken);
    }

    public void setAuthorizedSession(final String authorizationToken, final T session) {
        _sessions.put(authorizationToken, session);
    }
}
