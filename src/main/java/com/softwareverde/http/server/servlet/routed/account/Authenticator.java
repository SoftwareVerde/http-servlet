package com.softwareverde.http.server.servlet.routed.account;

import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.routed.Environment;
import com.softwareverde.http.server.servlet.session.Session;

import java.util.Map;

public interface Authenticator<E extends Environment> {
    AuthenticationResult authenticateUser(final Request request, final E environment, final Map<String, String> parameters) throws Exception;

    ResetPasswordResult resetPassword(final Session session, final Request request, final E environment, final Map<String, String> parameters) throws Exception;

    AccountInformationResult getAccountInformation(final Session session, Request request, E environment, Map<String, String> parameters);
}
