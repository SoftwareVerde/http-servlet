package com.softwareverde.http.server.servlet.request;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class InetSocketAddressHostInformation implements HostInformation {
    final InetSocketAddress _inetSocketAddress;

    public InetSocketAddressHostInformation(final InetSocketAddress inetSocketAddress) {
        _inetSocketAddress = inetSocketAddress;
    }
    @Override
    public String resolveHostName() {
        return _inetSocketAddress.getHostName();
    }

    @Override
    public boolean isHostNameResolved() {
        return (! _inetSocketAddress.isUnresolved());
    }

    @Override
    public String getHostInfo() {
        return _inetSocketAddress.toString();
    }

    @Override
    public String getIpAddress() {
        final InetAddress inetAddress = _inetSocketAddress.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    @Override
    public int getPort() {
        return _inetSocketAddress.getPort();
    }
}
