package com.softwareverde.http.server.servlet.request;

import com.softwareverde.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * <p>Container for HTTP headers.  The header names are stored in lower-case to allow for case-insensitive matching.</p>
 *
 * <p>Particularly because Tomcat automatically converts header to lower-case, the original form of header names cannot
 * be guaranteed.  To combat this, and take a consistent approach in all settings, this class forces the header names to
 * be lower-case regardless of whether or not Tomcat is used.</p>
 */
public class Headers {
    private final HashMap<String, List<String>> _headers;

    public Headers() {
        _headers = new HashMap<String, List<String>>();
    }

    public Headers(final Headers headers) {
        this();
        _headers.putAll(headers._headers);
    }

    public Set<String> getHeaderNames() {
        return _headers.keySet();
    }

    public boolean containsHeader(final String header) {
        if (header == null) {
            return false;
        }
        final String lowerCaseHeader = header.toLowerCase();
        return _headers.containsKey(lowerCaseHeader);
    }

    public List<String> getHeader(final String header) {
        if (header == null) {
            return null;
        }
        final String lowerCaseHeader = header.toLowerCase();
        final List<String> values = _headers.get(lowerCaseHeader);
        if (values == null) {
            return null;
        }
        else {
            return Util.copyList(values);
        }
    }

    public void setHeader(final String header, final List<String> values) {
        final String lowerCaseHeader = header.toLowerCase();
        if (values == null) {
            _headers.put(lowerCaseHeader, null);
        }
        else {
            final List<String> headerValues = Util.copyList(values);
            _headers.put(lowerCaseHeader, headerValues);
        }
    }

    public void clear() {
        _headers.clear();
    }
}
