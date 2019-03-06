package com.softwareverde.http.tls.sni;

import com.softwareverde.util.ByteUtil;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.nio.ByteBuffer;

public class SslEngineWithSni extends SSLEngine {
    public static class SessionProperties {
        public static final String SNI_KEY = "SERVER_NAME_SNI";
    }

    public static final byte HANDSHAKE_MESSAGE_TYPE = 0x16;
    public static final byte HANDSHAKE_MESSAGE_CLIENT_HELLO = 0x01;
    public static final int SERVER_NAME_EXTENSION = 0x00;
    public static final int headerByteCount = 5;
    public static final int maxMessageLength = (1 << 16);

    protected final SSLEngine _core;

    protected String _domainName = null;

    public SslEngineWithSni(final SSLEngine sslEngine) {
        super(sslEngine.getPeerHost(), sslEngine.getPeerPort());
        _core = sslEngine;
    }

    public String getDomainName() {
        return _domainName;
    }

    @Override
    public SSLEngineResult wrap(final ByteBuffer[] byteBuffers, final int index, final int length, final ByteBuffer byteBuffer) throws SSLException {
        return _core.wrap(byteBuffers, index, length, byteBuffer);
    }

    @Override
    public synchronized SSLEngineResult unwrap(final ByteBuffer byteBuffer, final ByteBuffer[] appData, final int index, final int length) throws SSLException {
        try {
            final int position = byteBuffer.position();
            final byte[] bytes = byteBuffer.array();

            final byte messageType = bytes[position];
            // final byte[] version = ByteUtil.copyBytes(bytes, position + 1, 2);
            final int messageLength = ByteUtil.bytesToInteger(ByteUtil.copyBytes(bytes, position + 3, 2));
            if ( (messageType == HANDSHAKE_MESSAGE_TYPE) && (messageLength < maxMessageLength) ) {
                final byte[] handshakeMessage = ByteUtil.copyBytes(bytes, (position + headerByteCount), (messageLength - headerByteCount));
                final byte handshakeMessageType = handshakeMessage[0];
                int currentIndex = 1;

                if (handshakeMessageType == HANDSHAKE_MESSAGE_CLIENT_HELLO) {
                    currentIndex += 3; // Handshake Message Length....
                    currentIndex += 2; // SSL/TLS Version
                    currentIndex += 32; // Random Nonce...

                    final int sessionIdByteCount = ByteUtil.bytesToInteger(ByteUtil.copyBytes(handshakeMessage, currentIndex, 1));
                    currentIndex += (sessionIdByteCount + 1);

                    final int ciphersByteCount = ByteUtil.bytesToInteger(ByteUtil.copyBytes(handshakeMessage, currentIndex, 2));
                    currentIndex += (ciphersByteCount + 2);

                    final int compressionMethodsByteCount = ByteUtil.bytesToInteger(ByteUtil.copyBytes(handshakeMessage, currentIndex, 1));
                    currentIndex += (compressionMethodsByteCount + 1);

                    final boolean hasExtensions = (currentIndex < messageLength);
                    if (hasExtensions) {
                        currentIndex += 2; // Total Extensions Byte Count...

                        do {
                            final int extensionId = ByteUtil.bytesToInteger(ByteUtil.copyBytes(handshakeMessage, currentIndex, 2));
                            currentIndex += 2;

                            final int extensionLength = ByteUtil.bytesToInteger(ByteUtil.copyBytes(handshakeMessage, currentIndex, 2));
                            currentIndex +=2 ;

                            if (extensionId == SERVER_NAME_EXTENSION) {
                                currentIndex += 2; // Server Name Extension Byte Count...
                                currentIndex += 1; // Server Name Type...

                                final int nameByteCount = ByteUtil.bytesToInteger(ByteUtil.copyBytes(handshakeMessage, currentIndex, 2));
                                currentIndex += 2;
                                if (nameByteCount > (maxMessageLength)) { break; }

                                final byte[] serverNameBytes = ByteUtil.copyBytes(handshakeMessage, currentIndex, nameByteCount);
                                currentIndex += nameByteCount;

                                _domainName = new String(serverNameBytes);
                                final SSLSession sslSession = _core.getSession();
                                sslSession.putValue(SessionProperties.SNI_KEY, _domainName);
                                break;
                            }
                            else {
                                currentIndex += extensionLength;
                            }
                        } while (currentIndex < messageLength);
                    }
                }
            }
        }
        catch (final Exception exception) {
            exception.printStackTrace();
        }

        return _core.unwrap(byteBuffer, appData, index, length);
    }

    @Override
    public Runnable getDelegatedTask() {
        return _core.getDelegatedTask();
    }

    @Override
    public void closeInbound() throws SSLException {
        _core.closeInbound();
    }

    @Override
    public boolean isInboundDone() {
        return _core.isInboundDone();
    }

    @Override
    public void closeOutbound() {
        _core.closeOutbound();
    }

    @Override
    public boolean isOutboundDone() {
        return _core.isOutboundDone();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return _core.getSupportedCipherSuites();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return _core.getEnabledCipherSuites();
    }

    @Override
    public void setEnabledCipherSuites(final String[] strings) {
        _core.setEnabledCipherSuites(strings);
    }

    @Override
    public String[] getSupportedProtocols() {
        return _core.getSupportedProtocols();
    }

    @Override
    public String[] getEnabledProtocols() {
        return _core.getEnabledProtocols();
    }

    @Override
    public void setEnabledProtocols(final String[] strings) {
        _core.setEnabledProtocols(strings);
    }

    @Override
    public SSLSession getSession() {
        return _core.getSession();
    }

    @Override
    public void beginHandshake() throws SSLException {
        _core.beginHandshake();
    }

    @Override
    public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        return _core.getHandshakeStatus();
    }

    @Override
    public void setUseClientMode(final boolean useClientMode) {
        _core.setUseClientMode(useClientMode);
    }

    @Override
    public boolean getUseClientMode() {
        return _core.getUseClientMode();
    }

    @Override
    public void setNeedClientAuth(final boolean needClientAuth) {
        _core.setNeedClientAuth(needClientAuth);
    }

    @Override
    public boolean getNeedClientAuth() {
        return _core.getNeedClientAuth();
    }

    @Override
    public void setWantClientAuth(final boolean wantClientAuth) {
        _core.setWantClientAuth(wantClientAuth);
    }

    @Override
    public boolean getWantClientAuth() {
        return _core.getWantClientAuth();
    }

    @Override
    public void setEnableSessionCreation(final boolean enableSessionCreation) {
        _core.setEnableSessionCreation(enableSessionCreation);
    }

    @Override
    public boolean getEnableSessionCreation() {
        return _core.getEnableSessionCreation();
    }
}
