package com.softwareverde.http.tls;

import com.softwareverde.security.AuthorizationKeyFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TlsFactory {

    protected final KeyStore _keyStore;
    protected final char[] _keyStorePassword;
    protected final List<Certificate> _certificates = new ArrayList<Certificate>();

    public TlsFactory() {
        KeyStore keyStore = null;
        char[] keyStorePassword = null;

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null); // Create an empty store.

            final AuthorizationKeyFactory authorizationKeyFactory = new AuthorizationKeyFactory();
            keyStorePassword = authorizationKeyFactory.generateAuthorizationKey().toCharArray();
        }
        catch (final Exception exception) { exception.printStackTrace(); }

        _keyStore = keyStore;
        _keyStorePassword = keyStorePassword;
    }

    public void addTlsCertificate(final String certificateContents, final byte[] p12Key) {
        try {
            final Certificate[] certificateChain;
            {
                final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                try (final BufferedInputStream certificateInputStream = new BufferedInputStream(new ByteArrayInputStream(certificateContents.getBytes()))) {
                    certificateChain = certificateFactory.generateCertificates(certificateInputStream).toArray(new java.security.cert.Certificate[0]);
                }

                for (final Certificate certificate : certificateChain) {
                    final X509Certificate x509Certificate = ((X509Certificate) certificate);
                    final String domainName = TlsCertificate.getHostName(x509Certificate);
                    if (domainName == null) { continue; }

                    _keyStore.setCertificateEntry(domainName, certificate);

                    // Alias the www certificates with the root certificate...
                    if (domainName.startsWith("www.")) {
                        _keyStore.setCertificateEntry(domainName.substring(4), certificate);
                    }
                }
            }

            if (p12Key != null) {
                final KeyStore privateKeyKeyStore = KeyStore.getInstance("PKCS12");
                try (final InputStream keyFileInputStream = new ByteArrayInputStream(p12Key)) {
                    privateKeyKeyStore.load(keyFileInputStream, null);
                }

                final Enumeration<String> keyAliases = privateKeyKeyStore.aliases();
                while (keyAliases.hasMoreElements()) {
                    final String alias = keyAliases.nextElement();
                    if (privateKeyKeyStore.isKeyEntry(alias)) {
                        final Key privateKey = privateKeyKeyStore.getKey(alias, null);
                        for (final Certificate certificate : certificateChain) {
                            final X509Certificate x509Certificate = ((X509Certificate) certificate);
                            final String domainName = TlsCertificate.getHostName(x509Certificate);
                            if (domainName == null) { continue; }

                            _keyStore.setKeyEntry(domainName, privateKey, _keyStorePassword, certificateChain);

                            // Alias the www certificates with the root certificate...
                            if (domainName.startsWith("www.")) {
                                _keyStore.setKeyEntry(domainName.substring(4), privateKey, _keyStorePassword, certificateChain);
                            }
                        }
                    }
                }
            }

            for (final Certificate certificate : certificateChain) {
                _certificates.add(certificate);
            }
        }
        catch (final Exception exception) {
            throw new TlsFactoryException(exception);
        }
    }

    public TlsCertificate buildCertificate() {
        try {
            KeyManagerFactory keyManagerFactory;
            TrustManagerFactory trustManagerFactory;

            try {
                keyManagerFactory = KeyManagerFactory.getInstance("X509");
                trustManagerFactory = TrustManagerFactory.getInstance("X509");
            }
            catch (final NoSuchAlgorithmException noSuchAlgorithmException) {
                keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            }

            keyManagerFactory.init(_keyStore, _keyStorePassword);
            trustManagerFactory.init(_keyStore);

            final TlsCertificate tlsCertificate = new TlsCertificate();
            tlsCertificate._keyManagerFactory = keyManagerFactory;
            tlsCertificate._trustManagerFactory = trustManagerFactory;
            tlsCertificate._certificates.addAll(_certificates);
            return tlsCertificate;
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}