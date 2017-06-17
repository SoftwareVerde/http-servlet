package com.softwareverde.httpserver.endpoint;

import com.softwareverde.servlet.Servlet;
import com.softwareverde.servlet.request.Request;
import com.softwareverde.servlet.response.Response;

public class EncryptionRedirectEndpoint implements Servlet {
    public static final Integer standardHttpsPort = 443;

    protected Integer _tlsPort = 443;
    public void setTlsPort(final Integer port) { _tlsPort = port; }
    public Integer getTlsPort() { return _tlsPort; }

    @Override
    public Response onRequest(final Request request) {
        final Response response = new Response();

        final String newUrl;
        {
            final String host = request.getHostname();
            final String url = request.getFilePath() + request.getQueryString();
            final Boolean requiresTlsPort = (! _tlsPort.equals(standardHttpsPort));
            newUrl = host + (requiresTlsPort ? ":"+ _tlsPort : "") + url;
        }

        response.addHeader("Location", "https://"+ newUrl);
        response.setCode(Response.ResponseCodes.MOVED_PERMANENTLY);
        response.setContent("Location: https://"+ newUrl);
        return response;
    }
}
