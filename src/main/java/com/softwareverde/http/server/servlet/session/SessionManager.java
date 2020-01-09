package com.softwareverde.http.server.servlet.session;

import com.softwareverde.constable.bytearray.MutableByteArray;
import com.softwareverde.http.cookie.Cookie;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.json.Json;
import com.softwareverde.util.IoUtil;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.Util;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    public static final String SESSION_COOKIE_KEY = "authentication_token";
    public static final int DEFAULT_SESSION_TIMEOUT = (int) TimeUnit.HOURS.toSeconds(24);

    public static Cookie createCookie(final String key, final String value, final Boolean shouldCreateSecureCookie, final Integer cookieMaxAgeInSeconds) {
        final Cookie cookie = new Cookie();
        cookie.setIsHttpOnly(true);
        cookie.setIsSameSiteStrict(true);
        cookie.setIsSecure(shouldCreateSecureCookie);
        cookie.setPath("/");
        cookie.setKey(key);
        cookie.setValue(value);
        cookie.setMaxAge(cookieMaxAgeInSeconds);
        return cookie;
    }

    public static SessionId getSessionId(final Request request) {
        final List<Cookie> cookies = request.getCookies();
        for (final Cookie cookie : cookies) {
            final String cookieKey = cookie.getKey();
            if (Util.areEqual(SESSION_COOKIE_KEY, cookieKey)) {
                final String sessionId = Util.coalesce(cookie.getValue()).replaceAll("[^0-9A-Za-z]", "");
                return SessionId.wrap(sessionId);
            }
        }

        return null;
    }

    protected final String _cookiesDirectory;
    protected final Boolean _shouldCreateSecureCookies;
    protected final Integer _cookieMaxAgeInSeconds;

    public SessionManager(final String cookiesDirectory, final Boolean shouldCreateSecureCookies) {
        this(cookiesDirectory, shouldCreateSecureCookies, DEFAULT_SESSION_TIMEOUT);
    }

    public SessionManager(final String cookiesDirectory, final Boolean shouldCreateSecureCookies, final Integer cookieMaxAgeInSeconds) {
        _cookiesDirectory = cookiesDirectory;
        _shouldCreateSecureCookies = shouldCreateSecureCookies;
        _cookieMaxAgeInSeconds = cookieMaxAgeInSeconds;

        final File cookiesDirectoryFile = new File(cookiesDirectory);
        if (! cookiesDirectoryFile.exists()) {
            if (! cookiesDirectoryFile.mkdir()) {
                throw new RuntimeException("Unable to create cookies directory.");
            }
        }
    }

    public Session getSession(final Request request) {
        final SessionId sessionId = SessionManager.getSessionId(request);
        if (sessionId == null) { return null; }

        final File cookieFile = new File(_cookiesDirectory + sessionId);
        if (! cookieFile.exists()) { return null; }

        final String sessionData = StringUtil.bytesToString(IoUtil.getFileContents(cookieFile));
        if (sessionData.isEmpty()) { return null; }

        return Session.newSession(sessionId, sessionData);
    }

    public Session createSession(final Request request, final Response response) {
        return createSession(request, response, _cookieMaxAgeInSeconds);
    }

    public Session createSession(final Request request, final Response response, final int cookieMaxAgeInSeconds) {
        final SecureRandom secureRandom = new SecureRandom();
        final MutableByteArray authenticationToken = new MutableByteArray(SessionId.BYTE_COUNT);
        secureRandom.nextBytes(authenticationToken.unwrap());

        final Session session = Session.newSession(SessionId.wrap(authenticationToken.toString()));
        final Json sessionData = session.getMutableData();

        IoUtil.putFileContents(_cookiesDirectory + session.getSessionId(), StringUtil.stringToBytes(sessionData.toString()));

        final Cookie sessionCookie = SessionManager.createCookie(SESSION_COOKIE_KEY, authenticationToken.toString(), _shouldCreateSecureCookies, cookieMaxAgeInSeconds);

        response.addCookie(sessionCookie);

        return session;
    }

    public void saveSession(final Session session) {
        IoUtil.putFileContents(_cookiesDirectory + session.getSessionId(), StringUtil.stringToBytes(session.toString()));
    }

    public boolean destroySession(final Request request, final Response response) {
        final SessionId sessionId = SessionManager.getSessionId(request);
        if (sessionId == null) { return false; }

        try {
            final File file = new File(_cookiesDirectory + sessionId);
            if (! file.exists() || ! file.delete()) {
                return false;
            }
        }
        catch (final Exception exception) {
            return false;
        }

        final Cookie sessionCookie = SessionManager.createCookie(SESSION_COOKIE_KEY, "", _shouldCreateSecureCookies, 0);
        sessionCookie.setMaxAge(0, true);
        response.addCookie(sessionCookie);

        return true;
    }
}