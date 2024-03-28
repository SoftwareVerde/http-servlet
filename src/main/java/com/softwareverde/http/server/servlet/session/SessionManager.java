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

    public static class CookieSettings {
        public static final Integer DEFAULT_COOKIE_MAX_AGE_IN_SECONDS = 31540000;
        public static final CookieSettings SecureCookieSettings = new CookieSettings();

        public Boolean isHttpOnly = true;
        public Boolean isSameSiteStrict = true;
        public Boolean shouldSetSecureFlag = true;
        public Integer cookieMaxAgeInSeconds = DEFAULT_COOKIE_MAX_AGE_IN_SECONDS;

        public CookieSettings() { }

        public CookieSettings(final Boolean shouldSetSecureFlag, final Integer cookieMaxAgeInSeconds) {
            this(true, true, shouldSetSecureFlag, cookieMaxAgeInSeconds);
        }

        public CookieSettings(final Boolean isHttpOnly, final Boolean isSameSiteStrict, final Boolean shouldSetSecureFlag, final Integer cookieMaxAgeInSeconds) {
            this.isHttpOnly = isHttpOnly;
            this.isSameSiteStrict = isSameSiteStrict;
            this.shouldSetSecureFlag = shouldSetSecureFlag;
            this.cookieMaxAgeInSeconds = cookieMaxAgeInSeconds;
        }

        public CookieSettings(final CookieSettings cookieSettings) {
            this.isHttpOnly = cookieSettings.isHttpOnly;
            this.isSameSiteStrict = cookieSettings.isSameSiteStrict;
            this.shouldSetSecureFlag = cookieSettings.shouldSetSecureFlag;
            this.cookieMaxAgeInSeconds = cookieSettings.cookieMaxAgeInSeconds;
        }

    }

    public static Cookie createCookie(final String key, final String value, final Boolean shouldCreateSecureCookie, final Integer cookieMaxAgeInSeconds) {
        final CookieSettings cookieSettings = new CookieSettings();
        cookieSettings.shouldSetSecureFlag = shouldCreateSecureCookie;
        cookieSettings.cookieMaxAgeInSeconds = cookieMaxAgeInSeconds;
        return SessionManager.createCookie(key, value, cookieSettings);
    }

    public static Cookie createCookie(final String key, final String value, final CookieSettings cookieSettings) {
        final Cookie cookie = new Cookie();
        cookie.setIsHttpOnly(Util.coalesce(cookieSettings.isHttpOnly));
        cookie.setIsSameSiteStrict(Util.coalesce(cookieSettings.isSameSiteStrict));
        cookie.setIsSecure(Util.coalesce(cookieSettings.shouldSetSecureFlag));
        cookie.setPath("/");
        cookie.setKey(key);
        cookie.setValue(value);
        cookie.setMaxAge(Util.coalesce(cookieSettings.cookieMaxAgeInSeconds, CookieSettings.DEFAULT_COOKIE_MAX_AGE_IN_SECONDS));
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
    protected final CookieSettings _sessionCookieSettings;

    public SessionManager(final String cookiesDirectory, final Boolean shouldCreateSecureCookies) {
        this(cookiesDirectory, shouldCreateSecureCookies, DEFAULT_SESSION_TIMEOUT);
    }

    public SessionManager(final String cookiesDirectory, final Boolean shouldCreateSecureCookies, final Integer cookieMaxAgeInSeconds) {
        this(cookiesDirectory, new CookieSettings(shouldCreateSecureCookies, cookieMaxAgeInSeconds));
    }

    public SessionManager(final String cookiesDirectory, final CookieSettings cookieSettings) {
        _cookiesDirectory = cookiesDirectory;

        _sessionCookieSettings = cookieSettings;

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
        return createSession(request, response, _sessionCookieSettings.cookieMaxAgeInSeconds);
    }

    public Session createSession(final Request request, final Response response, final int cookieMaxAgeInSeconds) {
        final SecureRandom secureRandom = new SecureRandom();
        final MutableByteArray authenticationToken = new MutableByteArray(SessionId.BYTE_COUNT);
        secureRandom.nextBytes(authenticationToken.unwrap());

        final Session session = Session.newSession(SessionId.wrap(authenticationToken.toString()));
        final Json sessionData = session.getMutableData();

        IoUtil.putFileContents(_cookiesDirectory + session.getSessionId(), StringUtil.stringToBytes(sessionData.toString()));

        final CookieSettings cookieSettings = new CookieSettings(_sessionCookieSettings);
        cookieSettings.cookieMaxAgeInSeconds = cookieMaxAgeInSeconds;

        final Cookie sessionCookie = SessionManager.createCookie(SESSION_COOKIE_KEY, authenticationToken.toString(), cookieSettings);

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

        final Cookie sessionCookie = SessionManager.createCookie(SESSION_COOKIE_KEY, "", _sessionCookieSettings.shouldSetSecureFlag, 0);
        sessionCookie.setMaxAge(0, true);
        response.addCookie(sessionCookie);

        return true;
    }
}