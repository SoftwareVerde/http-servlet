package com.softwareverde.httpserver;

import java.util.ArrayList;
import java.util.List;

class QueryStringParameter {
    private String _value;
    private List<String> _values;

    QueryStringParameter() { }
    QueryStringParameter(final String value) { _value = value; }

    void setValue(final String value) { _value = value; }
    void addValue(final String value) {
        if (_values == null) {
            _values = new ArrayList<String>();
        }

        _values.add(value);
    }

    Boolean isArray() { return (_values != null); }
    String getValue() { return _value; }
    List<String> getValues() { return _values; }
}