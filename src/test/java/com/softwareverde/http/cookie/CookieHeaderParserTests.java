package com.softwareverde.http.cookie;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CookieHeaderParserTests {
    @Test
    public void should_parse_single_cookie() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader("key1=value1");

        // Assert
        final Cookie cookie = cookies.get(0);
        Assert.assertEquals("key1", cookie.getKey());
        Assert.assertEquals("value1", cookie.getValue());
    }

    @Test
    public void should_parse_multiple_cookies() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader("key1=value1; key2=value2");

        // Assert
        Assert.assertEquals(2, cookies.size());

        final Cookie cookie0 = cookies.get(0);
        Assert.assertEquals("key1", cookie0.getKey());
        Assert.assertEquals("value1", cookie0.getValue());

        final Cookie cookie1 = cookies.get(1);
        Assert.assertEquals("key2", cookie1.getKey());
        Assert.assertEquals("value2", cookie1.getValue());
    }

    @Test
    public void should_parse_invalid_cookie() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader("key");

        // Assert
        Assert.assertEquals(0, cookies.size());
    }

    @Test
    public void should_parse_invalid_cookie_2() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader("=");

        // Assert
        Assert.assertEquals(0, cookies.size());
    }

    @Test
    public void should_parse_invalid_cookie_3() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader(";=");

        // Assert
        Assert.assertEquals(0, cookies.size());
    }

    @Test
    public void should_parse_invalid_cookie_4() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader(";");

        // Assert
        Assert.assertEquals(0, cookies.size());
    }

    @Test
    public void should_parse_empty_cookie() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromCookieHeader("key=");

        // Assert
        Assert.assertEquals(1, cookies.size());

        final Cookie cookie = cookies.get(0);
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("", cookie.getValue());
    }
}
