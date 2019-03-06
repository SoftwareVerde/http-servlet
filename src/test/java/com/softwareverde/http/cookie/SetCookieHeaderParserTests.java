package com.softwareverde.http.cookie;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SetCookieHeaderParserTests {
    @Test
    public void should_parse_empty_header() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromSetCookieHeader("");

        // Assert
        Assert.assertEquals(true, cookies.isEmpty());
    }

    @Test
    public void should_parse_simple_header() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
    }

    @Test
    public void should_parse_with_hyphenated_name() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("cookie-name=value").get(0);

        // Assert
        Assert.assertEquals("cookie-name", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
    }

    @Test
    public void should_parse_key_with_empty_value() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("", cookie.getValue());
    }

    @Test
    public void should_parse_key_with_empty_value_2() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=;").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("", cookie.getValue());
    }

    @Test
    public void should_parse_key_with_empty_value_3() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key= ;").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("", cookie.getValue());
    }

    @Test
    public void should_parse_key_with_empty_value_4() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key = ").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("", cookie.getValue());
    }

    @Test
    public void should_parse_key_with_empty_value_5() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key = ; Secure; HttpOnly").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_quoted_value() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=\"value\"").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_quoted_value_2() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=\" value \"").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals(" value ", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_quoted_value_3() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=\"test value\"").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("test value", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_quoted_value_4() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=\"; ").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("\"", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_quoted_value_5() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=\"_").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("\"_", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_valid_expiration() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; Expires=Wed, 21 Oct 2015 07:28:00 GMT").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("Wed, 21 Oct 2015 07:28:00 GMT", cookie.getExpirationDate());
    }

    @Test
    public void should_parse_cookie_with_invalid_expiration() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; Expires=2000-01-01; Secure; HttpOnly").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("2000-01-01", cookie.getExpirationDate());
    }

    @Test
    public void should_parse_with_no_spaces_between_segments() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value;Expires=Sat, 1 Jan 2000 00:00:00 GMT;Max-Age= 60;Domain=softwareverde.com;Path=/;Secure;HttpOnly;SameSite=Strict").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("Sat, 1 Jan 2000 00:00:00 GMT", cookie.getExpirationDate());
        Assert.assertEquals(60, cookie.getMaxAge().intValue());
        Assert.assertEquals("softwareverde.com", cookie.getDomain());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals(true, cookie.isSecure());
        Assert.assertEquals(true, cookie.isHttpOnly());
        Assert.assertEquals(true, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_regardless_of_attribute_order() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; SameSite=Strict;Max-Age= 60; HttpOnly; Secure; Expires=Sat, 1 Jan 2000 00:00:00 GMT; Domain=softwareverde.com; Path=/").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("Sat, 1 Jan 2000 00:00:00 GMT", cookie.getExpirationDate());
        Assert.assertEquals(60, cookie.getMaxAge().intValue());
        Assert.assertEquals("softwareverde.com", cookie.getDomain());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals(true, cookie.isSecure());
        Assert.assertEquals(true, cookie.isHttpOnly());
        Assert.assertEquals(true, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_all_valid_fields() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; Expires=Sat, 1 Jan 2000 00:00:00 GMT; Max-Age= 60; Domain=softwareverde.com; Path=/; Secure; HttpOnly; SameSite=Strict").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("Sat, 1 Jan 2000 00:00:00 GMT", cookie.getExpirationDate());
        Assert.assertEquals(60, cookie.getMaxAge().intValue());
        Assert.assertEquals("softwareverde.com", cookie.getDomain());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals(true, cookie.isSecure());
        Assert.assertEquals(true, cookie.isHttpOnly());
        Assert.assertEquals(true, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_invalid_segment_value() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; Expires=; Secure; Domain=").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("", cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals("", cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(true, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_lax_samesite() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; SameSite=Lax").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals(null, cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_omitted_flags() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals(null, cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_partial_invalid_syntax() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; Max-Age: 60; Domain: softwareverde.com s;dfusdlfdsfdf987yrhj231;").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals(null, cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_pure_commas() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromSetCookieHeader(",,,,,,,,,,,,,,");

        // Assert
        Assert.assertEquals(true, cookies.isEmpty());
    }

    @Test
    public void should_parse_cookie_with_cookie_followed_by_pure_commas() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromSetCookieHeader("key=value,,,,,,,,,,,,,,,");

        // Assert
        Assert.assertEquals(1, cookies.size());

        final Cookie cookie = cookies.get(0);
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
    }

    @Test
    public void should_parse_cookie_with_invalid_syntax() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromSetCookieHeader("invalid: sYntAx %823042: @%^#$*&^%$");

        // Assert
        Assert.assertEquals(true, cookies.isEmpty());
    }

    @Test
    public void should_parse_cookie_with_segment_value_null() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; Expires=").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("", cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_samesite_segment_empty() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; SameSite").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals(null, cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_cookie_with_samesite_segment_empty_2() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie cookie = cookieParser.parseFromSetCookieHeader("key=value; SameSite=").get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals(null, cookie.getExpirationDate());
        Assert.assertEquals(null, cookie.getMaxAge());
        Assert.assertEquals(null, cookie.getDomain());
        Assert.assertEquals(null, cookie.getPath());
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isHttpOnly());
        Assert.assertEquals(false, cookie.isSameSiteStrict());
    }

    @Test
    public void should_serialize_and_inflate_with_all_fields() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final Cookie sourceCookie = cookieParser.parseFromSetCookieHeader("key=value; Expires=Sat, 1 Jan 2000 00:00:00 GMT; Max-Age= 60; Domain=softwareverde.com; Path=/; Secure; HttpOnly; SameSite=Strict").get(0);
        final Cookie cookie = cookieParser.parseFromSetCookieHeader(sourceCookie.toString()).get(0);

        // Assert
        Assert.assertEquals("key", cookie.getKey());
        Assert.assertEquals("value", cookie.getValue());
        Assert.assertEquals("Sat, 1 Jan 2000 00:00:00 GMT", cookie.getExpirationDate());
        Assert.assertEquals(60, cookie.getMaxAge().intValue());
        Assert.assertEquals("softwareverde.com", cookie.getDomain());
        Assert.assertEquals("/", cookie.getPath());
        Assert.assertEquals(true, cookie.isSecure());
        Assert.assertEquals(true, cookie.isHttpOnly());
        Assert.assertEquals(true, cookie.isSameSiteStrict());
    }

    @Test
    public void should_parse_two_cookies_squashed_by_commas() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromSetCookieHeader("qwerty=219ffwef9w0f; Domain=somecompany.co.uk; Path=/; Expires=Wed, 30 Aug 2019 00:00:00 GMT, key=value; Expires=Sat, 1 Jan 2000 00:00:00 GMT; Max-Age= 60; Domain=softwareverde.com; Path=/; Secure; HttpOnly; SameSite=Strict");

        // Assert
        Assert.assertEquals(2, cookies.size());

        final Cookie cookie0 = cookies.get(0);
        Assert.assertEquals("qwerty", cookie0.getKey());
        Assert.assertEquals("219ffwef9w0f", cookie0.getValue());
        Assert.assertEquals("Wed, 30 Aug 2019 00:00:00 GMT", cookie0.getExpirationDate());
        Assert.assertEquals(null, cookie0.getMaxAge());
        Assert.assertEquals("somecompany.co.uk", cookie0.getDomain());
        Assert.assertEquals("/", cookie0.getPath());
        Assert.assertEquals(false, cookie0.isSecure());
        Assert.assertEquals(false, cookie0.isHttpOnly());
        Assert.assertEquals(false, cookie0.isSameSiteStrict());

        final Cookie cookie1 = cookies.get(1);
        Assert.assertEquals("key", cookie1.getKey());
        Assert.assertEquals("value", cookie1.getValue());
        Assert.assertEquals("Sat, 1 Jan 2000 00:00:00 GMT", cookie1.getExpirationDate());
        Assert.assertEquals(60, cookie1.getMaxAge().intValue());
        Assert.assertEquals("softwareverde.com", cookie1.getDomain());
        Assert.assertEquals("/", cookie1.getPath());
        Assert.assertEquals(true, cookie1.isSecure());
        Assert.assertEquals(true, cookie1.isHttpOnly());
        Assert.assertEquals(true, cookie1.isSameSiteStrict());
    }

    @Test
    public void should_parse_squashed_cookies_with_dates() {
        // Setup
        final CookieParser cookieParser = new CookieParser();

        // Action
        final List<Cookie> cookies = cookieParser.parseFromSetCookieHeader("key1=value1; Secure; Expires=Wed, 21 Oct 2015 07:28:00 GMT; Path=/, key2=value2; Secure; Expires=Wed, 21 Oct 2015 07:28:00 GMT; Path=/");

        // Assert
        final Cookie cookie0 = cookies.get(0);
        Assert.assertEquals("key1", cookie0.getKey());
        Assert.assertEquals("value1", cookie0.getValue());
        Assert.assertEquals("Wed, 21 Oct 2015 07:28:00 GMT", cookie0.getExpirationDate());
        Assert.assertEquals(true, cookie0.isSecure());
        Assert.assertEquals("/", cookie0.getPath());

        final Cookie cookie1 = cookies.get(1);
        Assert.assertEquals("key2", cookie1.getKey());
        Assert.assertEquals("value2", cookie1.getValue());
        Assert.assertEquals("Wed, 21 Oct 2015 07:28:00 GMT", cookie1.getExpirationDate());
        Assert.assertEquals(true, cookie1.isSecure());
        Assert.assertEquals("/", cookie1.getPath());
    }
}
