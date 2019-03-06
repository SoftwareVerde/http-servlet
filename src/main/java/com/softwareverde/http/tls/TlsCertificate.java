package com.softwareverde.http.tls;

import com.softwareverde.http.tls.sni.KeyManagerWithSni;
import com.softwareverde.http.tls.sni.ServerSslContextWithSni;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.Util;

import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class TlsCertificate {
    public static String getHostName(final X509Certificate x509Certificate) {
        final X500Principal x500Principal = x509Certificate.getSubjectX500Principal();
        final String subjectName = x500Principal.getName();
        final List<String> domainNameMatches = StringUtil.pregMatch("^.*CN=([^,]+).*$", subjectName);
        if (domainNameMatches.size() == 0) { return null; }
        return domainNameMatches.get(0);
    }

    public static List<String> getHostNames(final TlsCertificate tlsCertificate) {
        final ArrayList<String> hostNames = new ArrayList<String>();
        for (final Certificate certificate : tlsCertificate.getCertificates()) {
            final X509Certificate x509Certificate = (X509Certificate) certificate;
            final String domainName = TlsCertificate.getHostName(x509Certificate);
            if (domainName == null) { continue; }
            hostNames.add(domainName);
        }
        return hostNames;
    }

    protected final List<Certificate> _certificates = new ArrayList<Certificate>();

    protected KeyManagerFactory _keyManagerFactory;
    protected TrustManagerFactory _trustManagerFactory;

    public List<Certificate> getCertificates() {
        return Util.copyList(_certificates);
    }

    /**
     * Returns an SNI-aware SSL context for the associated x509 Certificates.
     *  Unlike the standard Java implementation, the certificate returned by this context will attempt to match
     *      the certificate with the domain requested.  If SNI is unavailable, then the default behavior is used.
     */
    public SSLContext createContext() {
        try {
            final SSLContext sslContext = ServerSslContextWithSni.newInstance();
            if (sslContext == null) { return null; }

            final ArrayList<KeyManager> keyManagerList = new ArrayList<KeyManager>();
            final ArrayList<TrustManager> trustManagerList = new ArrayList<TrustManager>();

            for (final KeyManager keyManager : _keyManagerFactory.getKeyManagers()) {
                if (! (keyManager instanceof X509KeyManager)) { continue; }

                final X509KeyManager coreKeyManager = (X509KeyManager) keyManager;
                keyManagerList.add(new KeyManagerWithSni(coreKeyManager));
            }

            for (final TrustManager trustManager : _trustManagerFactory.getTrustManagers()) {
                trustManagerList.add(trustManager);
            }

            sslContext.init(keyManagerList.toArray(new KeyManager[0]), trustManagerList.toArray(new TrustManager[0]), null);
            return sslContext;
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            throw new TlsFactoryException(exception);
        }
    }
}