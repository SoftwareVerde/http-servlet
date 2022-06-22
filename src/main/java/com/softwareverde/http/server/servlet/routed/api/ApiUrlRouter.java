package com.softwareverde.http.server.servlet.routed.api;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Tuple;

import java.util.*;

public class ApiUrlRouter<T> {
    protected static String _cleanUrl(final String url) {
        final String cleanPath = url.replaceAll("/[/]+", "/");
        return cleanPath;
    }

    private final String _baseUrl;
    private final Map<ApiUrl, T> _apiUrls = new LinkedHashMap<>();
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
        final List<Tuple<ApiRoute<T>, ApiUrl.MatchType>> matches = new ArrayList<>();

        for (final ApiUrl apiUrl : _apiUrls.keySet()) {
            final ApiUrl.MatchType matchType = apiUrl.matches(path, httpMethod);

            if (matchType != ApiUrl.MatchType.NONE) {
                final Map<String, String> urlParameters = apiUrl.getParameters(path);

                final T matchingApiUrl = _apiUrls.get(apiUrl);
                final ApiRoute<T> matchingApiRoute = new ApiRoute<T>(matchingApiUrl, urlParameters);

                matches.add(new Tuple<>(matchingApiRoute, matchType));
            }
        }

        if (matches.isEmpty()) {
            return new ApiRoute<T>(_errorApiRoute, null);
        }

        if (matches.size() == 1) {
            return matches.get(0).first;
        }

        final Tuple<ApiRoute<T>, ApiUrl.MatchType> exactMatch = matches
                .stream()
                .filter(apiRouteMatchTypeTuple -> apiRouteMatchTypeTuple.second == ApiUrl.MatchType.EXACT)
                .findFirst()
                .orElse(null);

        // If there are multiple matches that are not exact, it is likely that two similar routes with PARAMETER-type
        // segments at the same indices were added in error, e.g. /accounts/<accountId> and /accounts/<staffRole>.
        // The first match will be returned and an error will be logged.
        if (exactMatch == null) {
            final ApiRoute<T> firstRoute = matches.get(0).first;
            Logger.warn(String.format("Multiple routes found with matching variable URLs. Using %s", firstRoute.getRequestHandler()));

            return firstRoute;
        }

        return exactMatch.first;
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
