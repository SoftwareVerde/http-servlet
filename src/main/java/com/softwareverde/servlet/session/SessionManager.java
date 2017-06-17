package com.softwareverde.servlet.session;

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
    protected String _authorizationTokenKey = "authorization_token";
    protected String _sessionHeaderKey = "SESSION_ID";

    public void setAuthorizationTokenKey(final String authorizationTokenKey) {
        _authorizationTokenKey = authorizationTokenKey;
    }
    public String getAuthorizationTokenKey() {
        return _authorizationTokenKey;
    }

    public void setSessionHeaderKey(final String sessionHeaderKey) {
        _sessionHeaderKey = sessionHeaderKey;
    }
    public String getSessionHeaderKey() {
        return _sessionHeaderKey;
    }

    public String generateNewAuthorizationToken() {
        return _authorizationKeyFactory.generateAuthorizationKey();
    }

    /**
     * Returns T set by setAuthorizedSession() that has authorized by either the auth cookie or post parameters.
     *  Returns null if the session is not authorized.
     */
    public T getSession(final Request request) {
        final PostParameters postParameters = request.getPostParameters();
        final Map<String, List<String>> headers = request.getHeaders();

        final String authorizationToken;
        {
            if (headers.containsKey(_sessionHeaderKey)) {
                final List<String> sessionCookies = headers.get(_sessionHeaderKey);
                if (sessionCookies.isEmpty()) {
                    authorizationToken = "";
                }
                else {
                    authorizationToken = sessionCookies.get(0).trim();
                }
            }
            else {
                authorizationToken = Util.coalesce(postParameters.get(_authorizationTokenKey)).trim();
            }
        }

        if (authorizationToken.length() == 0) { return null; }

        return _sessions.get(authorizationToken);
    }

    public void setAuthorizedSession(final String authorizationToken, final T session) {
        _sessions.put(authorizationToken, session);
    }
}
