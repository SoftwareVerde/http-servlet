package com.softwareverde.security;

import com.softwareverde.util.Base58Util;

import java.security.SecureRandom;

public class AuthorizationKeyFactory {
    private final Integer _authTokenLength;
    private final SecureRandom _secureRandom = new SecureRandom();

    public AuthorizationKeyFactory(final Integer authTokenLength) {
        _authTokenLength = authTokenLength;
    }

    public AuthorizationKeyFactory() {
        _authTokenLength = 64;
    }

    public String generateAuthorizationKey() {
        final byte[] authTokenBytes = new byte[_authTokenLength];
        _secureRandom.nextBytes(authTokenBytes);
        return Base58Util.toBase58String(authTokenBytes);
    }
}
