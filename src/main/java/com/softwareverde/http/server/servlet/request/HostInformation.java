package com.softwareverde.http.server.servlet.request;

public interface HostInformation {
    /**
     * Returns the name of the host if available.  Permitted to perform a reverse DNS lookup if necessary.
     */
    String resolveHostName();

    /**
     * Returns true if the name of the host has been resolved.
     */
    boolean isHostNameResolved();

    /**
     * Returns a representation of the host's information, with various formats possible.
     */
    @Deprecated
    String getHostInfo();

    /**
     * Returns the IP address of the host.
     */
    String getIpAddress();

    /**
     * Returns the port used for the connection.
     */
    int getPort();
}
