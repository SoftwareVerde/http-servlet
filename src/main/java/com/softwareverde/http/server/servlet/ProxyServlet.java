package com.softwareverde.http.server.servlet;

import com.softwareverde.constable.bytearray.ByteArray;
import com.softwareverde.constable.bytearray.MutableByteArray;
import com.softwareverde.http.HttpRequest;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.http.querystring.GetParameters;
import com.softwareverde.http.server.servlet.request.Headers;
import com.softwareverde.http.server.servlet.request.WebSocketRequest;
import com.softwareverde.http.server.servlet.response.WebSocketResponse;
import com.softwareverde.http.websocket.WebSocket;
import com.softwareverde.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyServlet implements WebSocketServlet {
    public static class Url {
        public final String protocol;
        public final String hostname;
        public final Integer port;

        public Url(final String protocol, final String hostname, final Integer port) {
            this.protocol = protocol;
            this.hostname = hostname;
            this.port = port;
        }
    }

    protected final ConcurrentHashMap<Long, WebSocket> _proxiedWebSockets = new ConcurrentHashMap<Long, WebSocket>();
    protected final HashMap<String, Url> _proxyConfiguration = new HashMap<String, Url>();

    public ProxyServlet(final Map<String, Url> proxyConfiguration) {
        if (proxyConfiguration != null) {
            for (final String regex : proxyConfiguration.keySet()) {
                final Url proxiedUrl = proxyConfiguration.get(regex);
                _proxyConfiguration.put(regex, proxiedUrl);
            }
        }
    }

    @Override
    public WebSocketResponse onRequest(final WebSocketRequest request) {
        final HttpRequest httpRequest = new HttpRequest();

        final String serverHostname;
        {
            final Headers headers = request.getHeaders();
            if (headers.containsHeader("host")) {
                serverHostname = Util.coalesce(headers.getHeader("host").get(0));
            }
            else {
                serverHostname = request.getLocalHostInformation().resolveHostName();
            }
        }

        String url = null;
        for (final String hostnameRegex : _proxyConfiguration.keySet()) {
            final Pattern pattern = Pattern.compile(hostnameRegex);
            final Matcher matcher = pattern.matcher(serverHostname);
            if (matcher.matches()) {
                final Url proxiedUrl = _proxyConfiguration.get(hostnameRegex);
                url = (proxiedUrl.protocol + "://" + proxiedUrl.hostname + ":" + proxiedUrl.port);
                break;
            }
        }
        httpRequest.setUrl(url + request.getFilePath());

        httpRequest.setMethod(request.getMethod());
        httpRequest.setFollowsRedirects(true);
        httpRequest.setValidateSslCertificates(false);

        final Headers headers = request.getHeaders();
        for (final String headerKey : headers.getHeaderNames()) {
            String separator = "";
            final StringBuilder headerBuilder = new StringBuilder();
            for (final String headerValue : headers.getHeader(headerKey)) {
                headerBuilder.append(separator);
                headerBuilder.append(headerValue);
                separator = "; ";
            }

            httpRequest.setHeader(headerKey, headerBuilder.toString());
        }

        final GetParameters getParameters = request.getGetParameters();
        final ByteArray postData = MutableByteArray.wrap(request.getRawPostData());
        httpRequest.setQueryString(getParameters.toString());
        httpRequest.setRequestData(postData);

        final Boolean isWebSocketRequest = request.isWebSocketRequest();
        if (isWebSocketRequest) {
            httpRequest.setAllowWebSocketUpgrade(true);
        }

        final HttpResponse httpResponse = httpRequest.execute();

        final WebSocketResponse response = new WebSocketResponse();
        response.setCode(httpResponse.getResponseCode());

        final Map<String, List<String>> proxiedHeaders = httpResponse.getHeaders();
        if (proxiedHeaders != null) {
            for (final String headerKey : proxiedHeaders.keySet()) {
                for (final String headerValue : proxiedHeaders.get(headerKey)) {
                    if (headerKey != null) {
                        if (! Util.areEqual("sec-websocket-accept", headerKey.toLowerCase())) { // NOTE: Accept Headers for proxied socket should be omitted since they're handled by the upgrade...
                            response.addHeader(headerKey, headerValue);
                        }
                    }
                }
            }
        }

        if (httpResponse.didUpgradeToWebSocket()) {
            final WebSocket webSocket = httpResponse.getWebSocket();
            final Long webSocketId = webSocket.getId();

            _proxiedWebSockets.put(webSocketId, webSocket);

            response.setWebSocketId(webSocketId);
            response.upgradeToWebSocket();
        }
        else {
            final ByteArray rawResult = httpResponse.getRawResult();
            response.setContent((rawResult != null ? rawResult.getBytes() : new byte[0]));
        }

        return response;
    }

    @Override
    public void onNewWebSocket(final WebSocket externalWebSocket) {
        final WebSocket proxiedWebSocket = _proxiedWebSockets.get(externalWebSocket.getId());
        if (proxiedWebSocket == null) {
            externalWebSocket.close();
            return;
        }

        proxiedWebSocket.setMessageReceivedCallback(new WebSocket.MessageReceivedCallback() {
            @Override
            public void onMessage(final String message) {
                externalWebSocket.sendMessage(message);
            }
        });

        proxiedWebSocket.setBinaryMessageReceivedCallback(new WebSocket.BinaryMessageReceivedCallback() {
            @Override
            public void onMessage(final byte[] bytes) {
                externalWebSocket.sendMessage(bytes);
            }
        });

        proxiedWebSocket.setConnectionClosedCallback(new WebSocket.ConnectionClosedCallback() {
            @Override
            public void onClose(final int code, final String message) {
                _proxiedWebSockets.remove(externalWebSocket.getId());
                externalWebSocket.close();
            }
        });

        externalWebSocket.setMessageReceivedCallback(new WebSocket.MessageReceivedCallback() {
            @Override
            public void onMessage(final String message) {
                proxiedWebSocket.sendMessage(message);
            }
        });

        externalWebSocket.setBinaryMessageReceivedCallback(new WebSocket.BinaryMessageReceivedCallback() {
            @Override
            public void onMessage(final byte[] bytes) {
                proxiedWebSocket.sendMessage(bytes);
            }
        });

        externalWebSocket.setConnectionClosedCallback(new WebSocket.ConnectionClosedCallback() {
            @Override
            public void onClose(final int code, final String message) {
                proxiedWebSocket.close();
            }
        });

        proxiedWebSocket.startListening();
        externalWebSocket.startListening();
    }
}
