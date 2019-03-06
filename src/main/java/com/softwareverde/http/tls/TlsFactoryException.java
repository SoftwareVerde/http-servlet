package com.softwareverde.http.tls;

public class TlsFactoryException extends RuntimeException {
    protected final Exception _originalException;
    protected TlsFactoryException(final Exception exception) {
        super("Error loading SSL/TLS Certificate: "+ exception.getMessage());
        _originalException = exception;
    }

    public Exception getRootException() {
        return _originalException;
    }
}
