package com.softwareverde.http.server.servlet.routed.account;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.routed.json.JsonRequestHandler;
import com.softwareverde.http.server.servlet.session.Session;
import com.softwareverde.http.server.servlet.session.SessionManager;
import com.softwareverde.json.Json;

import java.util.Map;

public class AccountInformationRequestHandler<E extends Environment> extends JsonRequestHandler<E> {
    private final SessionManager _sessionManager;
    private final Authenticator<E> _authenticator;

    public AccountInformationRequestHandler(final SessionManager sessionManager, final Authenticator<E> authenticator) {
        _sessionManager = sessionManager;
        _authenticator = authenticator;
    }

    @Override
    public Json handleJsonRequest(final Request request, final E environment, final Map<String, String> parameters) throws Exception {
        final Session session = _sessionManager.getSession(request);
        if (session == null) {
            return _getUnauthenticatedResponse();
        }

        final Json sessionData = session.getMutableData();
        if (! sessionData.hasKey(LoginRequestHandler.ACCOUNT_SESSION_KEY)) {
            return _getUnauthenticatedResponse();
        }

        final AccountInformationResult accountInformationResult = _authenticator.getAccountInformation(session, request, environment, parameters);
        if (! accountInformationResult.wasSuccess()) {
            return JsonRequestHandler.generateErrorJson("Unable to collect account information: " + accountInformationResult.getErrorMessage());
        }

        final Json accountJson = accountInformationResult.getResult().toJson();

        final Json json = JsonRequestHandler.generateSuccessJson();
        json.put("account", accountJson);
        return json;
    }

    private Json _getUnauthenticatedResponse() {
        return JsonRequestHandler.generateErrorJson("Unauthenticated.");
    }
}
