package com.softwareverde.http.server.servlet;

import com.softwareverde.http.server.servlet.request.Headers;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.util.Util;

import java.util.List;

public class EncryptionRedirectServlet implements Servlet {
    public static final Integer standardHttpsPort = 443;

    protected Integer _tlsPort = 443;
    public void setTlsPort(final Integer port) { _tlsPort = port; }
    public Integer getTlsPort() { return _tlsPort; }

    protected String _getServerHostnameWithPort(final Request request) {
        final Headers headers = request.getHeaders();
        if (headers.containsHeader("host")) {
            final List<String> headerValues = headers.getHeader("host");
            final String hostHeaderValue = Util.coalesce(headerValues.get(0));
            if (! hostHeaderValue.isEmpty()) {
                return hostHeaderValue;
            }
        }

        return request.resolveHostname();
    }

    protected String _getServerHostname(final Request request) {
        final String hostnameAndPort = _getServerHostnameWithPort(request);
        if (! hostnameAndPort.contains(":")) { return hostnameAndPort; }

        final int endIndex = hostnameAndPort.indexOf(':');
        return hostnameAndPort.substring(0, endIndex);
    }

    @Override
    public Response onRequest(final Request request) {
        final Response response = new Response();

        final String newUrl;
        {
            final String hostname = _getServerHostname(request);
            final String url = request.getFilePath() + Util.coalesce(request.getQueryString());
            final Boolean requiresTlsPort = (! _tlsPort.equals(standardHttpsPort));
            newUrl = hostname + (requiresTlsPort ? ":"+ _tlsPort : "") + url;
        }

        response.addHeader("Location", "https://"+ newUrl);
        response.setCode(Response.Codes.MOVED_PERMANENTLY);
        response.setContent("Location: https://"+ newUrl);
        return response;
    }
}
