package com.softwareverde.http.server.servlet.routed.account;

import com.softwareverde.json.Jsonable;

public class AccountInformationResult {
    private boolean _wasSuccess;
    private String _errorMessage;
    private Jsonable _result;

    public static AccountInformationResult success(final Jsonable result) {
        return new AccountInformationResult(result);
    }

    public static AccountInformationResult failure(final String errorMessage) {
        return new AccountInformationResult(errorMessage);
    }

    protected AccountInformationResult(final String errorMessage) {
        _wasSuccess = false;
        _errorMessage = errorMessage;
    }

    protected  AccountInformationResult(final Jsonable result) {
        _wasSuccess = true;
        _result = result;
    }

    public boolean wasSuccess() {
        return _wasSuccess;
    }

    public String getErrorMessage() {
        return _errorMessage;
    }

    public Jsonable getResult() {
        return _result;
    }
}
