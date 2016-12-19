package com.softwareverde.httpserver;

import com.softwareverde.util.Json;
import com.softwareverde.util.Util;

import java.util.HashMap;
import java.util.Map;

public class ContentTypeResolver {
    private static Map<String, String> _extensionContentMap = new HashMap<String, String>();
    static {
        final Json contentTypes = Json.fromString(Util.getResource("/content-types.json"));
        for (Integer i = 0; i < contentTypes.length(); ++i) {
            final Json contentType = contentTypes.get(i);
            _extensionContentMap.put(contentType.get(0, Json.Types.STRING), contentType.get(1, Json.Types.STRING));
        }
    }

    /**
     * Returns true if the extension is a recognized type.
     * @param extension - The file extension to look up; includes the period.
     */
    public Boolean isKnownExtension(final String extension) {
        return _extensionContentMap.containsKey(extension);
    }

    /**
     * Returns the MIME string representation of the extension provided.
     * @param extension - The file extension to look up; includes the period.
     */
    public String getContentType(final String extension) {
        if (! _extensionContentMap.containsKey(extension)) { return ""; }
        return _extensionContentMap.get(extension);
    }
}
