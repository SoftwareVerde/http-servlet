package com.softwareverde.http.server.servlet.routed;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.server.servlet.Servlet;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.api.ApiRoute;
import com.softwareverde.http.server.servlet.routed.api.ApiUrlRouter;
import com.softwareverde.logging.Logger;
import com.softwareverde.logging.LoggerInstance;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public abstract class RoutedServlet<E extends Environment, RequestHandler> implements Servlet {
    private final LoggerInstance _logger = Logger.getInstance(this.getClass());

    private final ApiUrlRouter<RequestHandler> _apiUrlRouter;
    private final E _environment;

    public RoutedServlet(final E environment) {
        this(environment, "/");
    }

    public RoutedServlet(final E environment, final String apiBaseUrl) {
        this(environment, apiBaseUrl, null);
    }

    public RoutedServlet(final E environment, final String apiBaseUrl, final RequestHandler errorApiRoute) {
        _environment = environment;
        if (errorApiRoute != null) {
            _apiUrlRouter = new ApiUrlRouter<>(apiBaseUrl, errorApiRoute);
        }
        else {
            _apiUrlRouter = new ApiUrlRouter<>(apiBaseUrl, null);
        }
    }

    protected void _defineEndpoint(final String endpointPattern, final HttpMethod httpMethod, final RequestHandler requestHandler) {
        _apiUrlRouter.defineEndpoint(endpointPattern, httpMethod, requestHandler);
    }

    protected abstract Response _handleRequest(final E environment, final Request request, final RequestHandler requestHandler, final Map<String, String> routeParameters) throws Exception;

    @Override
    public Response onRequest(final Request request) {
        final long startTime = System.currentTimeMillis();

        try {
            final ApiRoute<RequestHandler> route = _apiUrlRouter.route(request);
            final RequestHandler requestHandler = route.getRequestHandler();
            if (requestHandler == null) {
                _logger.warn("Null request handler for " + request.getFilePath());
                return _getBadRequestResponse();
            }
            final Response response = _handleRequest(_environment, request, requestHandler, route.getParameters());
            return response;
        }
        catch (final Exception e) {
            _logger.error("Unable to handle request.", e);
            return _getServerErrorResponse();
        }
        finally {
            final long requestTime = System.currentTimeMillis() - startTime;
            _logger.info("Processed request to " + request.getFilePath() + " in " + requestTime + "ms.");
        }
    }

    protected Response _getBadRequestResponse() {
        final Response response = new Response();
        response.setCode(HttpServletResponse.SC_BAD_REQUEST);
        response.setContent("Invalid request.");
        return response;
    }

    protected Response _getServerErrorResponse() {
        final Response response = new Response();
        response.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContent("Server error.");
        return response;
    }
}
