package com.softwareverde.example.server;

import com.softwareverde.httpserver.ApiServer;
import com.softwareverde.httpserver.GetParameters;
import com.softwareverde.httpserver.PostParameters;
import com.softwareverde.util.Json;

public class DemoApi extends ApiServer.Endpoint<Account> {
    public DemoApi(final ApiServer<Account> apiServer) {
        super(apiServer);
    }

    @Override
    public ApiServer.Response onRequest(final GetParameters getParameters, final PostParameters postParameters) {
        final ApiServer.Response response = new ApiServer.Response();

        response.setCode(ApiServer.Response.ResponseCodes.OK);

        final Json jsonResponse = new Json();
        jsonResponse.put("wasSuccess", true);

        { // Copy Get-Parameters to Json Response
            final Json getParametersJson = new Json();
            for (final String key : getParameters.getKeys()) {
                final String value = getParameters.get(key);
                getParametersJson.put(key, value);
            }
            jsonResponse.put("getParameters", getParametersJson);
        }

        { // Copy Post-Parameters to Json Response
            final Json postParametersJson = new Json();
            for (final String key : postParameters.getKeys()) {
                final String value = postParameters.get(key);
                postParametersJson.put(key, value);
            }
            jsonResponse.put("postParameters", postParametersJson);
        }

        response.setContent(jsonResponse.toString());

        return response;
    }
}
