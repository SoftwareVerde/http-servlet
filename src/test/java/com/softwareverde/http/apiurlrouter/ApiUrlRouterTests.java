package com.softwareverde.http.apiurlrouter;

import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.routed.api.ApiUrlRouter;
import com.softwareverde.http.server.tomcat.request.TomcatRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class ApiUrlRouterTests {
    private static final String GET_ALL_ACCOUNTS = "GET_ALL_ACCOUNTS";
    private static final String GET_ACCOUNT_BY_ID = "GET_ACCOUNT_BY_ID";
    private static final String UPDATE_ACCOUNT_BY_ID = "UPDATE_ACCOUNT_BY_ID";
    private static final String GET_ACCOUNT_NAME = "GET_ACCOUNT_NAME";
    private static final String UPDATE_ACCOUNT_NAME = "UPDATE_ACCOUNT_NAME";
    private static final String GET_ALL_ACCOUNT_TYPES = "GET_ALL_ACCOUNT_TYPES";
    private static final String CONFLICTING_URL = "CONFLICTING_URL";

    protected final ApiUrlRouter<String> _apiUrlRouter = new ApiUrlRouter<>("/", null);

    private Request _buildRequest(final String url, final HttpMethod httpMethod) {
        final TomcatRequest request = new TomcatRequest();
        request.setFilePath(url);
        request.setMethod(httpMethod);

        return request;
    }

    @Before
    public void setUp() {
        _apiUrlRouter.defineEndpoint("/accounts", HttpMethod.GET, GET_ALL_ACCOUNTS);
        _apiUrlRouter.defineEndpoint("/accounts/<accountId>", HttpMethod.GET, GET_ACCOUNT_BY_ID);
        _apiUrlRouter.defineEndpoint("/accounts/<accountId>", HttpMethod.POST, UPDATE_ACCOUNT_BY_ID);
        _apiUrlRouter.defineEndpoint("/accounts/<accountId>/name", HttpMethod.GET, GET_ACCOUNT_NAME);
        _apiUrlRouter.defineEndpoint("/accounts/<accountId>/name", HttpMethod.POST, UPDATE_ACCOUNT_NAME);
        _apiUrlRouter.defineEndpoint("/accounts/types", HttpMethod.GET, GET_ALL_ACCOUNT_TYPES);
        _apiUrlRouter.defineEndpoint("/accounts/<typeId>", HttpMethod.GET, CONFLICTING_URL);
    }

    @Test
    public void should_route_to_valid_request_handlers_and_prefer_exact_matches() {
        final Request getAllAccountsRequest = _buildRequest("/accounts", HttpMethod.GET);
        final Request getAccountByIdRequest = _buildRequest("/accounts/<accountId>", HttpMethod.GET);
        final Request updateAccountByIdRequest = _buildRequest("/accounts/<accountId>", HttpMethod.POST);
        final Request getAccountNameRequest = _buildRequest("/accounts/<accountId>/name", HttpMethod.GET);
        final Request updateAccountNameRequest = _buildRequest("/accounts/<accountId>/name", HttpMethod.POST);
        final Request getAllAccountTypesRequest = _buildRequest("/accounts/types", HttpMethod.GET);

        final List<Request> requests = Arrays.asList(
                getAllAccountsRequest,
                getAccountByIdRequest,
                updateAccountByIdRequest,
                getAccountNameRequest,
                updateAccountNameRequest,
                getAllAccountTypesRequest);

        final int expectedApiCalls = requests.size();

        try {
            final Map<String, Integer> handledRequestsCountByRoute = new HashMap<>();

            Assert.assertEquals(GET_ALL_ACCOUNTS, _apiUrlRouter.route(getAllAccountsRequest).getRequestHandler());
            handledRequestsCountByRoute.merge(GET_ALL_ACCOUNTS, 1, Integer::sum);

            Assert.assertEquals(GET_ACCOUNT_BY_ID, _apiUrlRouter.route(getAccountByIdRequest).getRequestHandler());
            handledRequestsCountByRoute.merge(GET_ACCOUNT_BY_ID, 1, Integer::sum);

            Assert.assertEquals(UPDATE_ACCOUNT_BY_ID, _apiUrlRouter.route(updateAccountByIdRequest).getRequestHandler());
            handledRequestsCountByRoute.merge(UPDATE_ACCOUNT_BY_ID, 1, Integer::sum);

            Assert.assertEquals(GET_ACCOUNT_NAME, _apiUrlRouter.route(getAccountNameRequest).getRequestHandler());
            handledRequestsCountByRoute.merge(GET_ACCOUNT_NAME, 1, Integer::sum);

            Assert.assertEquals(UPDATE_ACCOUNT_NAME, _apiUrlRouter.route(updateAccountNameRequest).getRequestHandler());
            handledRequestsCountByRoute.merge(UPDATE_ACCOUNT_NAME, 1, Integer::sum);

            Assert.assertEquals(GET_ALL_ACCOUNT_TYPES, _apiUrlRouter.route(getAllAccountTypesRequest).getRequestHandler());
            handledRequestsCountByRoute.merge(GET_ALL_ACCOUNT_TYPES, 1, Integer::sum);

            Assert.assertEquals(handledRequestsCountByRoute.size(), expectedApiCalls);

            handledRequestsCountByRoute.values().forEach(integer -> Assert.assertEquals(1, (int) integer));
        }
        catch (final Exception exception) {
            Assert.fail(exception.getMessage());
        }
    }

    @Test
    public void should_handle_conflicting_endpoint_by_returning_first_match() {
        final Request conflictingUrlRequest = _buildRequest("/accounts/<typeId>", HttpMethod.GET);

        try {
            Assert.assertEquals(GET_ACCOUNT_BY_ID, _apiUrlRouter.route(conflictingUrlRequest).getRequestHandler());
        }
        catch (final Exception exception) {
            Assert.fail(exception.getMessage());
        }
    }
}
