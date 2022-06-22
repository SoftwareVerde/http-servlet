package com.softwareverde.http.server.servlet.routed.api;

import com.softwareverde.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ApiUrl {
    protected enum SegmentType {
        SEGMENT, PARAMETER
    }

    protected enum MatchType {
        NONE, PARAMETERIZED, EXACT
    }

    private final String _path;
    private final HttpMethod _httpMethod;
    private final List<SegmentType> _segmentTypes = new ArrayList<SegmentType>();
    private final List<String> _segmentIdentifiers = new ArrayList<String>();

    public ApiUrl(final String path, final HttpMethod httpMethod) {
        _path = path;
        _httpMethod = httpMethod;
    }

    public void appendParameter(final String parameter) {
        _segmentTypes.add(SegmentType.PARAMETER);
        _segmentIdentifiers.add(parameter);
    }

    public void appendSegment(final String segment) {
        _segmentTypes.add(SegmentType.SEGMENT);
        _segmentIdentifiers.add(segment);
    }

    public MatchType matches(final String path, final HttpMethod httpMethod) {
        if (httpMethod != _httpMethod) { return MatchType.NONE; }

        final String cleanedPath = ApiUrlRouter._cleanUrl(path);
        final String[] segments = cleanedPath.split("/");
        if (_segmentTypes.size() != segments.length) { return MatchType.NONE; }

        boolean isExactMatch = true;

        for (int i=0; i<segments.length; ++i) {
            final String segment = segments[i];

            final boolean isSegment = (_segmentTypes.get(i) == SegmentType.SEGMENT);
            if (isSegment) {
                 final String segmentIdentifier = _segmentIdentifiers.get(i);
                 if (! segmentIdentifier.equalsIgnoreCase(segment)) {
                     return MatchType.NONE;
                 }
            }
            else {
                isExactMatch = false;
            }
        }

        return isExactMatch ? MatchType.EXACT : MatchType.PARAMETERIZED;
    }

    public Map<String, String> getParameters(final String path) {
        final Map<String, String> parameters = new HashMap<String, String>();

        final String cleanedPath = ApiUrlRouter._cleanUrl(path);

        final String[] segments = cleanedPath.split("/");
        if (_segmentTypes.size() != segments.length) { return null; }

        for (int i=0; i<segments.length; ++i) {
            final String parameterValue = segments[i];

            final boolean isParameter = (_segmentTypes.get(i) == SegmentType.PARAMETER);
            if (isParameter) {
                final String parameterKey = _segmentIdentifiers.get(i);
                parameters.put(parameterKey, parameterValue);
            }
        }

        return parameters;
    }

    @Override
    public boolean equals(final Object object) {
        if (! (object instanceof ApiUrl)) { return false; }
        final ApiUrl apiUrl = (ApiUrl) object;
        return _path.equals(apiUrl._path) && _httpMethod.equals(apiUrl._httpMethod);
    }

    @Override
    public int hashCode() {
        return _path.hashCode();
    }
}
