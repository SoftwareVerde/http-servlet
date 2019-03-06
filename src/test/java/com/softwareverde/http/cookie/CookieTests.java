package com.softwareverde.http.cookie;


import org.junit.Assert;
import org.junit.Test;

public class CookieTests {

    @Test
    public void should_strip_invalid_characters_from_cookies() {
        // Setup
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("|'");
        for (int i=0; i<32; ++i) {
            stringBuilder.append((char) i);
        }
        stringBuilder.append('\u007F');
        stringBuilder.append(' ');
        stringBuilder.append('\t');
        stringBuilder.append('"');
        stringBuilder.append(',');
        stringBuilder.append(';');
        stringBuilder.append('\\');
        stringBuilder.append("'|");
        final String valueWithInvalidCharacters = stringBuilder.toString();
        final String expectedValue = "|''|";

        final Cookie cookie = new Cookie();
        cookie.setKey("key");

        // Action
        cookie.setValue(valueWithInvalidCharacters);
        final String cookieValue = cookie.getValue();

        // Assert
        Assert.assertEquals(expectedValue, cookieValue);
    }
}
