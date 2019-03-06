package com.softwareverde.http.cookie;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CompileSetCookieHeaderTests {
    protected final DateFormat _cookieExpiresDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    @Before
    public void setUp() {
        _cookieExpiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Test
    public void should_compile_full_cookie() {
        // Setup
        final Integer maxAge = 60; // 1 Minute

        final String expectedDate = _cookieExpiresDateFormat.format(new Date(System.currentTimeMillis() + maxAge * 1000L));
        final String expectedHeaderValue = "id=a3fWa; Expires="+ expectedDate +"; Max-Age=60; Domain=softwareverde.com; Path=/; Secure; HttpOnly; SameSite=Strict";

        final Cookie cookie = new Cookie();
        cookie.setKey("id");
        cookie.setValue("a3fWa");
        cookie.setExpirationDate("Wed, 21 Oct 2015 07:28:00 GMT");
        cookie.setMaxAge(maxAge);
        cookie.setDomain("softwareverde.com");
        cookie.setPath("/");
        cookie.setIsSecure(true);
        cookie.setIsHttpOnly(true);
        cookie.setIsSameSiteStrict(true);

        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<String> receivedValues = cookieParser.compileCookiesIntoSetCookieHeaderValues(Collections.singletonList(cookie));

        // Assert
        Assert.assertEquals(1, receivedValues.size());

        Assert.assertEquals(expectedHeaderValue, receivedValues.get(0));
    }

    @Test
    public void should_update_expires_if_setting_max_age() {
        // Setup
        final Long now = System.currentTimeMillis();
        final Long expirationTime = now + 60L * 1000L;

        final Integer maxAge = 10;
        final String expectedMaxAge = _cookieExpiresDateFormat.format(new Date(now + maxAge * 1000L));

        final Cookie cookie = new Cookie("key", "value");
        cookie.setExpirationDate(expirationTime);

        // Action
        cookie.setMaxAge(maxAge);

        // Assert
        Assert.assertEquals(10, cookie.getMaxAge().intValue());
        Assert.assertEquals(expectedMaxAge, cookie.getExpirationDate());
    }

    @Test
    public void should_not_set_expires_if_unset_and_setting_max_age() {
        // Setup
        final Cookie cookie = new Cookie("key", "value");

        // Action
        cookie.setMaxAge(1);

        // Assert
        Assert.assertEquals(1, cookie.getMaxAge().intValue());
        Assert.assertEquals(null, cookie.getExpirationDate());
    }

    @Test
    public void should_not_change_expires_if_setting_max_age_to_null() {
        // Setup
        final Long now = System.currentTimeMillis();
        final String expectedExpirationDate = _cookieExpiresDateFormat.format(new Date(now));

        final Cookie cookie = new Cookie("key", "value");
        cookie.setExpirationDate(expectedExpirationDate);

        // Action
        cookie.setMaxAge(null);

        // Assert
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(expectedExpirationDate, cookie.getExpirationDate());
    }

    @Test
    public void should_not_change_max_age_if_setting_expires_to_null() {
        // Setup
        final Integer maxAge = 10;

        final Cookie cookie = new Cookie("key", "value");
        cookie.setMaxAge(maxAge);

        // Action
        cookie.setExpirationDate((String) null);

        // Assert
        Assert.assertEquals(maxAge, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getExpirationDate());
    }

    @Test
    public void should_not_change_max_age_if_setting_expires_to_null_2() {
        // Setup
        final Integer maxAge = 10;

        final Cookie cookie = new Cookie("key", "value");
        cookie.setMaxAge(maxAge);

        // Action
        cookie.setExpirationDate((Long) null);

        // Assert
        Assert.assertEquals(maxAge, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getExpirationDate());
    }

    @Test
    public void should_not_change_expires_if_setting_max_age_with_setExpirationDate_false() {
        // Setup
        final Long now = System.currentTimeMillis();
        final String expectedExpirationDate = _cookieExpiresDateFormat.format(new Date(now));

        final Cookie cookie = new Cookie("key", "value");
        cookie.setExpirationDate(expectedExpirationDate);

        // Action
        cookie.setMaxAge(10, false);

        // Assert
        Assert.assertEquals(10, cookie.getMaxAge().intValue());
        Assert.assertEquals(expectedExpirationDate, cookie.getExpirationDate());
    }

    @Test
    public void should_change_expires_if_setting_max_age_with_setExpirationDate_true() {
        // Setup
        final Integer maxAge = 10;

        final Long now = System.currentTimeMillis();
        final String expectedExpirationDate = _cookieExpiresDateFormat.format(new Date(now + (maxAge * 1000L)));

        final Cookie cookie = new Cookie("key", "value");

        // Action
        cookie.setMaxAge(maxAge, true);

        // Assert
        Assert.assertEquals(maxAge, cookie.getMaxAge());
        Assert.assertEquals(expectedExpirationDate, cookie.getExpirationDate());
    }

    @Test
    public void should_change_expires_if_setting_max_age_to_null_with_setExpirationDate_true() {
        // Setup
        final Long now = System.currentTimeMillis();
        final String expectedExpirationDate = _cookieExpiresDateFormat.format(new Date(now));

        final Cookie cookie = new Cookie("key", "value");
        cookie.setExpirationDate(expectedExpirationDate);

        // Action
        cookie.setMaxAge(null, true);

        // Assert
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getExpirationDate());
    }
}
