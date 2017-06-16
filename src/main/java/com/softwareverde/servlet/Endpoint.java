package com.softwareverde.servlet;

public class Endpoint {
    protected final Servlet _servlet;
    protected String _path;
    protected Boolean _shouldUseStrictPath;

    public Endpoint(final Servlet servlet) {
        _servlet = servlet;
    }

    public void setPath(final String path) {
        _path = path;
    }

    public String getPath() {
        return _path;
    }

    public void setStrictPathEnabled(final Boolean strictPathEnabled) {
        _shouldUseStrictPath = strictPathEnabled;
    }

    public Boolean shouldUseStrictPath() {
        return _shouldUseStrictPath;
    }

    public Servlet getServlet() {
        return _servlet;
    }
}
