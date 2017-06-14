package com.softwareverde.httpserver.endpoint;

import com.softwareverde.httpserver.request.Request;
import com.softwareverde.httpserver.response.JsonResult;
import com.softwareverde.httpserver.response.Response;

public class EncryptionRedirectEndpoint extends StaticContentHandler {
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
        response.setContent(new JsonResult(false, "Location: https://"+ newUrl).toJson().toString());
        return response;
    }
}
