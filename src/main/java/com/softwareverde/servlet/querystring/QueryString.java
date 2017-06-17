package com.softwareverde.servlet.querystring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QueryString {
    private Map<String, QueryStringParameter> _values = new HashMap<String, QueryStringParameter>();

    protected QueryString() { }

    protected void _setValues(final Map<String, QueryStringParameter> values) {
        _values = values;
    }

    public Set<String> getKeys() {
        return _values.keySet();
    }

    /**
     * Returns true if the key exists within the QueryString and is an array.
     *  Returns false if the key does not exist within the QueryString.
     */
    public Boolean isArray(final String key) {
        if (! _values.containsKey(key)) { return false; }
        return _values.get(key).isArray();
    }

    /**
     * Returns true if the key exists in the QueryString.
     *  NOTE: If the key exists, this function returns true regardless if the key is associated with a value or array.
     */
    public Boolean containsKey(final String key) { return _values.containsKey(key); }

    /**
     * Returns the value associated with the provided key.
     *  If the key does not exist, an empty string is returned.
     *  If the key is associated with an array, the first value of the array is returned.
     */
    public String get(final String key) {
        if (! _values.containsKey(key)) { return ""; }

        final QueryStringParameter queryStringParameter = _values.get(key);
        if (queryStringParameter.isArray()) {
            return queryStringParameter.getValues().get(0);
        }
        else {
            return queryStringParameter.getValue();
        }
    }

    /**
     * Returns the value(s) associated with the provided key.
     *  If the key does not exist, an empty array is returned.
     *  If the key is associated with a value, an array containing that value is turned.
     */
    public String[] getArray(final String key) {
        if (! _values.containsKey(key)) { return new String[0]; }

        final QueryStringParameter queryStringParameter = _values.get(key);
        if (queryStringParameter.isArray()) {
            return queryStringParameter.getValues().toArray(new String[0]);
        }
        else {
            return new String[]{ queryStringParameter.getValue() };
        }
    }
}