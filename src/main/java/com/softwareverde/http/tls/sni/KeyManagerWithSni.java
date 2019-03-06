package com.softwareverde.http.tls.sni;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * The standard Java KeyManager does not take into account the SNI provided when serving certifcates.
 * KeyManagerWithSni will attempt to server the x509 certificate with the correctly associated domain name.
 *  If the SNI is not available, either due to the request not having SNI or the SNI is otherwise unavailable,
 *      then the standard/default behavior is used.
 */
public class KeyManagerWithSni extends X509ExtendedKeyManager {
    protected final X509KeyManager _core;

    public KeyManagerWithSni(final X509KeyManager coreKeyManager) {
        _core = coreKeyManager;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        return _core.getClientAliases(keyType, issuers);
    }

    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        return _core.chooseClientAlias(keyType, issuers, socket);
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        return _core.getServerAliases(keyType, issuers);
    }

    @Override
    public String chooseServerAlias(final String ketType, final Principal[] issuer, final Socket socket) {
        return _core.chooseServerAlias(ketType, issuer, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        return _core.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
        return _core.getPrivateKey(alias);
    }

    @Override
    public String chooseEngineClientAlias(final String[] keyType, final Principal[] issuers, final SSLEngine engine) {
        if (_core instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) _core).chooseEngineClientAlias(keyType, issuers, engine);
        }
        return null;
    }

    @Override
    public String chooseEngineServerAlias(final String keyType, final Principal[] issuers, final SSLEngine sslEngine) {
        final SSLSession sslSession = sslEngine.getSession();
        final String serverNameSni = (String) sslSession.getValue(SslEngineWithSni.SessionProperties.SNI_KEY);
        if (serverNameSni != null) {
            final PrivateKey privateKey = _core.getPrivateKey(serverNameSni);
            if (privateKey != null) {
                return serverNameSni;
            }
        }

        if (_core instanceof X509ExtendedKeyManager) {
            final X509ExtendedKeyManager extendedCore = (X509ExtendedKeyManager) _core;
            return extendedCore.chooseEngineServerAlias(keyType, issuers, sslEngine);
        }

        return null;
    }
}