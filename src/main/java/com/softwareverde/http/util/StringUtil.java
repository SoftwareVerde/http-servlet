package com.softwareverde.http.util;

public class StringUtil extends com.softwareverde.util.StringUtil {
    protected StringUtil() { }

    /**
     * Removes a single set of double quotes from the provided string, if present at both the start and end.
     */
    public static String unquoteString(final String possiblyQuotedString) {
        return StringUtil.pregMatch("^(\"?)(.*)\\1$", possiblyQuotedString).get(1);
    }
}
