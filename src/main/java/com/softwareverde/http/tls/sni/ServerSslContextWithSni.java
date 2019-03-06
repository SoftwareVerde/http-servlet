package com.softwareverde.http.tls.sni;

import com.softwareverde.util.ReflectionUtil;

import javax.net.ssl.SSLContext;
import java.security.Provider;

public class ServerSslContextWithSni extends SSLContext {
    public static final String CORE_SPI_MEMBER_NAME = "contextSpi";
    public static final String PROVIDER_MEMBER_NAME = "provider";
    public static final String PROTOCOL_MEMBER_NAME = "protocol";

    /**
     * Attempts to create a ServerSslContextWithSni instance backed by the system default instance.
     * SNI may be accessible via ServerSslContextWithSni::createSSLEngine::getSession::getValue(SslEngineWithSni.SessionProperties.SNI_KEY)
     *  Since accessing the SNI can only be done via reflection, if the underlying implementation changes
     *      then it may not be able to instantiate a version with access to SNI.
     *  If the ServerSslContextWithSni cannot be created, the default instance is returned instead.
     *      In this scenario, the context will not provide access to SNI values.
     */
    public static SSLContext newInstance() {
        try {
            final SSLContext core = SSLContext.getInstance("TLS");

            try {
                return new ServerSslContextWithSni(core);
            }
            catch (final Exception exception) {
                System.out.println("NOTICE: Unable to create Server SSL Context with SNI. Using system default.");
                return core;
            }
        }
        catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    protected final SSLContext _core;
    protected final SslContextSpiWithSni _coreSpi;

    public ServerSslContextWithSni(final SSLContext core) {
        super(new SslContextSpiWithSni(core), (Provider) ReflectionUtil.getValue(core, PROVIDER_MEMBER_NAME), (String) ReflectionUtil.getValue(core, PROTOCOL_MEMBER_NAME));
        _core = core;
        _coreSpi = ReflectionUtil.getValue(this, CORE_SPI_MEMBER_NAME);
    }
}
