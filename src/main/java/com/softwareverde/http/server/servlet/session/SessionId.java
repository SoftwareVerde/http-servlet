package com.softwareverde.http.server.servlet.session;

public class SessionId {
    public static final Integer BYTE_COUNT = 32;

    public static SessionId wrap(final String sessionId) {
        if (sessionId == null) { return null; }
        if (sessionId.length() != (BYTE_COUNT * 2)) { return null; }

        return new SessionId(sessionId);
    }

    protected final String _value;

    protected SessionId(final String value) {
        _value = value;
    }

    @Override
    public int hashCode() {
        return _value.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (! (object instanceof SessionId)) { return false; }

        final SessionId sessionId = (SessionId) object;
        return _value.equals(sessionId._value);
    }

    @Override
    public String toString() {
        return _value;
    }
}
