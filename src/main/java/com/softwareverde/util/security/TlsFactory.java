package com.softwareverde.util.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class TlsFactory {
    public static TlsCertificate loadTlsCertificate(final String certificate, final byte[] p12Key) {
        final AuthorizationKeyFactory authorizationKeyFactory = new AuthorizationKeyFactory();
        final char[] temporaryKeyStorePassword = authorizationKeyFactory.generateAuthorizationKey().toCharArray();

        final TlsCertificate tlsCertificate = new TlsCertificate();

        try {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            {
                keyStore.load(null); // Create an empty store.

                final Certificate[] certificateChain;
                {
                    final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    final BufferedInputStream certificateInputStream = new BufferedInputStream(new ByteArrayInputStream(certificate.getBytes()));
                    certificateChain = certificateFactory.generateCertificates(certificateInputStream).toArray(new java.security.cert.Certificate[0]);
                    certificateInputStream.close();

                    Integer certificateIndex = 0;
                    for (final Certificate cert : certificateChain) {
                        final String alias = "certificate" + (certificateIndex > 0 ? certificateIndex : "");
                        keyStore.setCertificateEntry(alias, cert);
                        certificateIndex += 1;
                    }
                    tlsCertificate._certificates = certificateChain;
                }

                if (p12Key != null) {
                    final InputStream keyFileInputStream = new ByteArrayInputStream(p12Key);
                    final KeyStore privateKeyKeyStore = KeyStore.getInstance("PKCS12");
                    privateKeyKeyStore.load(keyFileInputStream, null);
                    keyFileInputStream.close();

                    final Enumeration<String> keyAliases = privateKeyKeyStore.aliases();
                    while (keyAliases.hasMoreElements()) {
                        final String alias = keyAliases.nextElement();
                        if (privateKeyKeyStore.isKeyEntry(alias)) {
                            final Key privateKey = privateKeyKeyStore.getKey(alias, null);
                            keyStore.setKeyEntry(alias, privateKey, temporaryKeyStorePassword, certificateChain);
                        }
                    }
                }
            }

            {
                KeyManagerFactory keyManagerFactory = null;
                TrustManagerFactory trustManagerFactory = null;

                try {
                    keyManagerFactory = KeyManagerFactory.getInstance("X509");
                    trustManagerFactory = TrustManagerFactory.getInstance("X509");
                }
                catch (final NoSuchAlgorithmException noSuchAlgorithmException) {
                    keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                    trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
                }

                keyManagerFactory.init(keyStore, temporaryKeyStorePassword);
                trustManagerFactory.init(keyStore);

                tlsCertificate._keyManagerFactory = keyManagerFactory;
                tlsCertificate._trustManagerFactory = trustManagerFactory;
            }

            return tlsCertificate;
        }
        catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading SSL Certificate.");
        }
    }

    public static SSLContext createContext(final TlsCertificate tlsCertificate) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(tlsCertificate._keyManagerFactory.getKeyManagers(), tlsCertificate._trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        }
        catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading SSL Certificate.");
        }
    }
}
