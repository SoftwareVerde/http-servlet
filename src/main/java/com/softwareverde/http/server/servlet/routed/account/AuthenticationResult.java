package com.softwareverde.http.server.servlet.routed.account;

import com.softwareverde.json.Json;

public class AuthenticationResult {
    private boolean _wasSuccess;
    private Json _accountJson;
    private String _errorMessage;

    public static AuthenticationResult success(final Json accountJson) {
        return new AuthenticationResult(accountJson);
    }

    public static AuthenticationResult failure(final String errorMessage) {
        return new AuthenticationResult(errorMessage);
    }

    /**
     * Creates a successful authentication result with the provided account details.
     * @param accountJson
     */
    protected AuthenticationResult(final Json accountJson) {
        _wasSuccess = true;
        _accountJson = accountJson;
    }

    /**
     * Creates an unsuccessful authentication result with the provided error message.
     * @param errorMessage
     */
    protected AuthenticationResult(final String errorMessage) {
        _wasSuccess = false;
        _errorMessage = errorMessage;
    }

    public boolean wasSuccess() {
        return _wasSuccess;
    }

    public Json getAccountJson() {
        return _accountJson;
    }

    public String getErrorMessage() {
        return _errorMessage;
    }
}
