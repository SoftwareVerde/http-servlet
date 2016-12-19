package com.softwareverde.util.security;

import java.security.cert.Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class TlsCertificate {
    protected Certificate[] _certificates;

    protected KeyManagerFactory _keyManagerFactory;
    protected TrustManagerFactory _trustManagerFactory;

    public Certificate[] getCertificates() {
        return _certificates;
    }
}
