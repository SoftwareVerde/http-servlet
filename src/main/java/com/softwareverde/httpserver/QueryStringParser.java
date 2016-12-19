package com.softwareverde.httpserver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class QueryStringParser<T extends QueryString> {
    public interface QueryStringFactory<T extends QueryString> {
        T newInstance();
    }

    private final QueryStringFactory<T> _queryStringFactory;

    private Boolean _isKeyForAnArray(final String key) {
        if ( (key == null) || (key.length() < 2) ) { return false; }
        return (key.substring(key.length() - 2).equals("[]"));
    }

    private String _transformArrayKey(final String key) {
        return key.substring(0, key.length() - 2);
    }

    public QueryStringParser(final QueryStringFactory<T> queryStringFactory) {
        _queryStringFactory = queryStringFactory;
    }

    public T parse(final String query) {
        final Map<String, QueryStringParameter> parameters = new HashMap<String, QueryStringParameter>();
        if (query != null) {
            final String pairs[] = query.split("[&]");

            for (final String pair : pairs) {
                final String keyValuePair[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (keyValuePair.length == 2) {
                    try {
                        key = URLDecoder.decode(keyValuePair[0], "UTF-8");
                        value = URLDecoder.decode(keyValuePair[1], "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                    }
                }

                if ( (key != null) && (value != null) ) {
                    if (_isKeyForAnArray(key)) {
                        final String arrayKey = _transformArrayKey(key);

                        final QueryStringParameter parameter;
                        if (parameters.containsKey(arrayKey)) {
                            parameter = parameters.get(arrayKey);
                        }
                        else {
                            parameter = new QueryStringParameter();
                        }

                        parameter.addValue(value);
                        parameters.put(arrayKey, parameter);
                    }
                    else {
                        parameters.put(key, new QueryStringParameter(value));
                    }
                }
            }
        }

        final T queryString = _queryStringFactory.newInstance();
        queryString.setValues(parameters);
        return queryString;
    }
}