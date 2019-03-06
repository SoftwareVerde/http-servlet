package com.softwareverde.http.tls.sni;

import com.softwareverde.util.ReflectionUtil;

import javax.net.ssl.*;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.SecureRandom;

public class SslContextSpiWithSni extends SSLContextSpi {
    protected final SSLContextSpi _core;

    public SslContextSpiWithSni(final SSLContext coreSslContext) {
        _core = ReflectionUtil.getValue(coreSslContext, ServerSslContextWithSni.CORE_SPI_MEMBER_NAME);
    }

    @Override
    protected void engineInit(final KeyManager[] keyManagers, final TrustManager[] trustManagers, final SecureRandom secureRandom) throws KeyManagementException {
        try {
            final Method method = SSLContextSpi.class.getDeclaredMethod("engineInit", KeyManager[].class, TrustManager[].class, SecureRandom.class);
            method.setAccessible(true);
            method.invoke(_core, keyManagers, trustManagers, secureRandom);
        }
        catch (final Exception exception) { exception.printStackTrace(); }
    }

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
        try {
            return ReflectionUtil.invoke(_core, "engineGetSocketFactory");
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        try {
            return ReflectionUtil.invoke(_core, "engineGetServerSocketFactory");
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    protected SSLEngine engineCreateSSLEngine() {
        try {
            final SSLEngine coreSSLEngine = ReflectionUtil.invoke(_core, "engineCreateSSLEngine");
            return new SslEngineWithSni(coreSSLEngine);
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(final String host, final int port) {
        try {
            final Method method = SSLContextSpi.class.getDeclaredMethod("engineCreateSSLEngine", String.class, int.class);
            method.setAccessible(true);
            final SSLEngine coreSSLEngine = (SSLEngine) (method.invoke(_core, host, port));
            return new SslEngineWithSni(coreSSLEngine);
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
        try {
            return ReflectionUtil.invoke(_core, "engineGetServerSessionContext");
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        try {
            return ReflectionUtil.invoke(_core, "engineGetClientSessionContext");
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}