package com.softwareverde.util;

import com.softwareverde.util.security.TlsCertificate;
import com.softwareverde.util.security.TlsFactory;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebRequest {
    private synchronized static void debug(String str) {
        System.out.println("com.softwareverde.util :: WebRequest :: "+ str);
    }

    public enum RequestType {
        POST, GET
    };

    public interface Callback {
        void run(WebRequest request);
    };

    private static Map<String, TlsCertificate> pinnedCertificates = new ConcurrentHashMap<String, TlsCertificate>();
    public static void setPinnedCertificate(final String hostname, final String certificate) {
        pinnedCertificates.put(hostname, TlsFactory.loadTlsCertificate(certificate, null));
    }

    private String _url;
    private Map<String, String> _getParams;
    private Map<String, String> _postParams;
    private Map<String, List<String>> _arrayGetParams;
    private Map<String, List<String>> _arrayPostParams;
    private boolean _isPost = true;
    private List<String> _setCookies = new LinkedList<String>();
    private Map<String, String> _setHeaders = new HashMap<String, String>();

    private Map<String, List<String>> _headers = null;

    private boolean _resultReady = false;
    private String _rawResult;
    private Integer _responseCode;
    private boolean _followsRedirects = false;

    // NOTE: Handles both android-formatted and ios-formatted cookie strings.
    //  iOS concatenates their cookies into one string, delimited by commas;
    //  Android cookies are separate cookie-records.
    private List<String> _parseCookies(final String cookie) {
        final List<String> cookies = new LinkedList<String>();

        if (cookie.contains(";")) {
            Boolean skipNext = false;
            for (final String cookieSegment : cookie.replaceAll(",", ";").split(";")) {
                if (skipNext) {
                    skipNext = false;
                    continue;
                }

                final String cleanedCookie = cookieSegment.trim();

                if (cleanedCookie.toLowerCase().contains("expires=")) {
                    skipNext = true;
                    continue;
                }
                if (cleanedCookie.toLowerCase().contains("max-age=")) {
                    continue;
                }
                if (cleanedCookie.toLowerCase().contains("path=")) {
                    continue;
                }
                if (cleanedCookie.toLowerCase().contains("httponly")) {
                    continue;
                }

                cookies.add(cleanedCookie);
            }
        }
        else {
            cookies.add(cookie.trim());
        }

        return cookies;
    }

    public WebRequest() {
        _getParams = new HashMap<String, String>();
        _postParams = new HashMap<String, String>();

        _arrayGetParams = new HashMap<String, List<String>>();
        _arrayPostParams = new HashMap<String, List<String>>();
    }
    public void setUrl(final String url) {
        _url = url;
    }
    public String getUrl() { return _url; }

    public void setGetParam(final String key, final String value) {
        if (key == null) { return; }

        if (value == null) {
            if (_getParams.containsKey(key)) {
                _getParams.remove(key);
            }
        }
        else {
            _getParams.put(key, value);
        }
    }

    public void setPostParam(final String key, final String value) {
        if (key == null) { return; }

        if (value == null) {
            if (_postParams.containsKey(key)) {
                _postParams.remove(key);
            }
        }
        else {
            _postParams.put(key, value);
        }
    }

    public void addGetParam(final String key, final String value) {
        if (! _arrayGetParams.containsKey(key)) {
            _arrayGetParams.put(key, new ArrayList<String>());
        }

        if (_getParams.containsKey(key)) {
            _getParams.remove(key);
        }

        final List<String> array = _arrayGetParams.get(key);
        array.add(value);
    }

    public void addPostParam(final String key, final String value) {
        if (! _arrayPostParams.containsKey(key)) {
            _arrayPostParams.put(key, new ArrayList<String>());
        }

        final List<String> array = _arrayPostParams.get(key);
        array.add(value);
    }

    public void setCookie(String cookie) {
        if (cookie.contains(";")) {
            cookie = cookie.substring(0, cookie.indexOf(";"));
        }
        _setCookies.add(cookie);
    }
    public void setHeader(final String key, final String value) {
        _setHeaders.put(key, value);
    }

    public void setFollowsRedirects(final boolean followsRedirects) {
        _followsRedirects = followsRedirects;
    }

    public void setType(final WebRequest.RequestType type) {
        switch (type) {
            case POST: {
                    _isPost = true;
                } break;
            case GET: {
                    _isPost = false;
                } break;
            default: break;
        }
    }

    public boolean hasResult() {
        return _resultReady;
    }
    public Integer getResponseCode() { return _responseCode; }

    public synchronized Json getJsonResult() {
        if (! _resultReady) return null;

        return Json.fromString(_rawResult);
    }
    public synchronized String getRawResult() { return _rawResult; }
    private synchronized void _setResult(final String result) {
        _rawResult = result;
        _resultReady = true;
    }

    public Map<String, List<String>> getHeaders() {
        if (! _resultReady) return null;

        return _headers;
    }

    public List<String> getCookies() {
        if (! _resultReady) return null;
        if (_headers.containsKey("Set-Cookie")) {
            List<String> cookies = new LinkedList<String>();
            for (final String cookie : _headers.get("Set-Cookie")) {
                cookies.addAll(_parseCookies(cookie));
            }

            return cookies;
        }

        return new LinkedList<String>();
    }

    public void execute(final Boolean nonblocking) {
        this.execute(nonblocking, null);
    }
    public void execute(final Callback callback) {
        this.execute(true, callback);
    }
    public void execute(final Boolean nonblocking, final Callback callback) {
        _resultReady = false;
        _rawResult = null;

        if (_url != null) {
            WebRequest.debug("Executing WebRequest: "+ _url);
            final ConnectionThread thread = new ConnectionThread(this, callback);
            if (nonblocking) {
                thread.start();
            }
            else {
                thread.run();
            }
        }
    }

    private void _checkForPinnedCertificates(final URL url, final HttpURLConnection connection) {
        if (url.getProtocol().equals("https")) {
            final HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            if (pinnedCertificates.containsKey(url.getHost())) {
                final TlsCertificate tlsCertificate = pinnedCertificates.get(url.getHost());
                httpsConnection.setSSLSocketFactory(TlsFactory.createContext(tlsCertificate).getSocketFactory());
                httpsConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(final String hostname, final SSLSession sslSession) {
                        try {
                            for (final Certificate certificate : sslSession.getPeerCertificates()) {
                                for (final Certificate pinnedCertificate : tlsCertificate.getCertificates()) {
                                    if (certificate.equals(pinnedCertificate)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        catch (final SSLPeerUnverifiedException e) { }
                        return false;
                    }
                });
            }
        }
    }

	private class ConnectionThread extends Thread {
        private boolean _isPost;
        private WebRequest _webRequest;
        private Callback _callback;

		public ConnectionThread(WebRequest webRequest, Callback callback) {
            _webRequest = webRequest;
            _callback = callback;

            _isPost = _webRequest._isPost;
		}

		public void run() {
			if (_webRequest._url == null) return;

            String get = "";
            try {
                for (final String key : _getParams.keySet()) {
                    final String value = _getParams.get(key);

                    get += "&";
                    get += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
                }

                for (final String key : _arrayGetParams.keySet()) {
                    final List<String> values = _arrayGetParams.get(key);

                    for (String value : values) {
                        if (value == null) {
                            continue;
                        }

                        get += "&";
                        get += URLEncoder.encode(key + "[]", "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
                    }
                }
            }
            catch (final UnsupportedEncodingException e) { WebRequest.debug("Exception 1: "+ e.getMessage()); }

            URL url = null;
            try {
                String address = _webRequest._url;
                if (!address.contains("?")) {
                    address += "?";
                }
                url = new URL(address + get);
            }
            catch (MalformedURLException e) {
                WebRequest.debug("Exception 2: " + e.getMessage());
            }

			try {
				final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                _checkForPinnedCertificates(url, connection);

				connection.setDoInput(true);
				connection.setInstanceFollowRedirects(_followsRedirects);
				connection.setUseCaches(false);

                String cookies = "";
                for (final String cookie : _setCookies) {
                    cookies += cookie +"; ";
                }
                connection.setRequestProperty("Cookie", cookies);

                for (final String key : _setHeaders.keySet()) {
                    final String value = _setHeaders.get(key);
                    connection.setRequestProperty(key, value);
                }

                if (_isPost) {
                    String post = "";
                    boolean first_param = true;

                    for (final String key : _postParams.keySet()) {
                        final String value = _postParams.get(key);

                        if (value == null) {
                            continue;
                        }

                        if (! first_param) post += "&";
                        post += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");

                        first_param = false;
                    }

                    for (final String key : _arrayPostParams.keySet()) {
                        final List<String> values = _arrayPostParams.get(key);

                        for (final String value : values) {
                            if (value == null) {
                                continue;
                            }

                            if (! first_param) post += "&";
                            post += URLEncoder.encode(key + "[]", "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");

                            first_param = false;
                        }
                    }

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Charset", "UTF-8");
                    connection.setRequestProperty("Content-Length", Integer.toString(post.length()));
                    connection.setDoOutput(true);

                    final DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(post);
                    out.flush();
                    out.close();
                }
                else {
                    connection.setRequestMethod("GET");
                }

                _responseCode = connection.getResponseCode();
                WebRequest.debug("Response Code: " + _responseCode);

                if (_responseCode >= 400) {
                    final String data = Util.streamToString(connection.getErrorStream()).trim();
                    _webRequest._setResult(data);

                    WebRequest.debug("Error Response: "+ _url +": "+ _rawResult);
                }
                else {
                    final String data = Util.streamToString(connection.getInputStream()).trim();
                    _webRequest._setResult(data);
                }

                _headers = connection.getHeaderFields();

				// Close Connection
				connection.disconnect();
			}
            catch (final UnknownHostException e) { WebRequest.debug("Exception 3: "+ e.getMessage()); }
            catch (final SSLException e) { WebRequest.debug("Exception 4: "+ e.getMessage()); }
            catch (final IOException e) { WebRequest.debug("Exception 5: "+ e.getMessage()); }
            catch (final Exception e) { WebRequest.debug("Exception 6: "+ e.getMessage()); }

            if (_callback != null) {
                _callback.run(_webRequest);
            }
		}
	}

    @SuppressWarnings("unused")
    private static final Class<?>[] unused = {
        HttpURLConnection.class,
        HttpsURLConnection.class
    };
}
