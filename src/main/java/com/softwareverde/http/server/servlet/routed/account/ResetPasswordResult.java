package com.softwareverde.http.server.servlet.routed.account;

public class ResetPasswordResult {
    private boolean _wasSuccess;
    private String _errorMessage;

    public static ResetPasswordResult success() {
        return new ResetPasswordResult();
    }

    public static ResetPasswordResult failure(final String errorMessage) {
        return new ResetPasswordResult(errorMessage);
    }

    /**
     * Creates a successful password reset result.
     */
    protected ResetPasswordResult() {
        _wasSuccess = true;
    }

    /**
     * Creates an unsuccessful password reset result with the provided error message.
     * @param errorMessage
     */
    protected ResetPasswordResult(final String errorMessage) {
        _wasSuccess = false;
        _errorMessage = errorMessage;
    }

    public boolean wasSuccess() {
        return _wasSuccess;
    }

    public String getErrorMessage() {
        return _errorMessage;
    }
}
