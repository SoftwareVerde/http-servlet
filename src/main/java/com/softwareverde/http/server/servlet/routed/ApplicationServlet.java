package com.softwareverde.http.server.servlet.routed;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;

import java.util.Map;

/**
 * <p>Manages an endpoint, or collection of endpoints, by allowing for the routing of requests by path and method to a
 * configured request handler.</p>
 *
 * @param <E> An environment object that will be passed to all requests.  This can be used as a container for database objects,
 *           configuration, and other objects that need to be accessible to requests are available to them.
 */
public abstract class ApplicationServlet<E extends Environment> extends RoutedServlet<E, RequestHandler<E>> {
    public ApplicationServlet(final E environment) {
        this(environment, "/", null);
    }

    public ApplicationServlet(final E environment, final String apiBaseUrl) {
        this(environment, apiBaseUrl, null);
    }

    public ApplicationServlet(final E environment, final String apiBaseUrl, final RequestHandler<E> errorApiRoute) {
        super(environment, apiBaseUrl, errorApiRoute);
    }

    @Override
    protected Response _handleRequest(final E environment, final Request request, final RequestHandler<E> requestHandler, final Map<String, String> routeParameters) throws Exception {
        return requestHandler.handleRequest(request, environment, routeParameters);
    }
}
