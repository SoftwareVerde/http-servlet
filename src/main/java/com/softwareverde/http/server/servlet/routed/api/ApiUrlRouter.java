package com.softwareverde.http.server.servlet.routed.api;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.server.servlet.request.Request;

import java.util.HashMap;
import java.util.Map;

public class ApiUrlRouter<T> {
    protected static String _cleanUrl(final String url) {
        final String cleanPath = url.replaceAll("/[/]+", "/");
        return cleanPath;
    }

    private final String _baseUrl;
    private final Map<ApiUrl, T> _apiUrls = new HashMap<>();
    private final T _errorApiRoute;

    public ApiUrlRouter(final String baseUrl, final T errorApiRoute) {
        _baseUrl = baseUrl;
        _errorApiRoute = errorApiRoute;
    }

    public void defineEndpoint(final String endpointPattern, final HttpMethod httpMethod, final T apiRoute) {
        final String path = _cleanUrl(_baseUrl + endpointPattern);
        final String[] segments = path.split("/");

        final ApiUrl apiUrl = new ApiUrl(_baseUrl + endpointPattern, httpMethod);

        for (int i=0; i<segments.length; ++i) {
            final String segment = segments[i];

            boolean isVariable = false;
            if ((segment.length() > 2)) {
                final Character firstCharacter = segment.charAt(0);
                final Character lastCharacter = segment.charAt(segment.length() - 1);

                if (firstCharacter.equals('<') && lastCharacter.equals('>')) {
                    final String variableName = segment.substring(1, segment.length() - 1);
                    apiUrl.appendParameter(variableName);
                    isVariable = true;
                }
            }

            if (! isVariable) {
                apiUrl.appendSegment(segment);
            }
        }

        _apiUrls.put(apiUrl, apiRoute);
    }

    public ApiRoute<T> route(final Request request) throws RouteNotFoundException {
        final HttpMethod httpMethod = request.getMethod();
        final String urlPath = getUrlPath(request);
        final String path = _cleanUrl(urlPath);

        for (final ApiUrl apiUrl : _apiUrls.keySet()) {
            if (apiUrl.matches(path, httpMethod)) {
                final Map<String, String> urlParameters = apiUrl.getParameters(path);

                final T apiRoute = _apiUrls.get(apiUrl);

                return new ApiRoute<T>(apiRoute, urlParameters);
            }
        }

        return new ApiRoute<T>(_errorApiRoute, null);
    }

    private String getUrlPath(final Request request) throws RouteNotFoundException {
        return request.getFilePath();

        // TODO: fix this in TomcatRequest?
//        try {
//            final String fullUrl = request.getFilePath();
//            URL url = new URL(fullUrl);
//            final String path = url.getPath();
//            return path;
//        } catch (final Exception exception) {
//            throw new RouteNotFoundException("Unable to determine route for " + request.getFilePath(), e);
//        }
    }
}
