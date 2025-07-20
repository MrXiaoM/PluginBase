/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package top.mrxiaom.pluginbase.resolver.http.impl.auth;

import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.HttpResponse;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthOption;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthProtocolState;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthScheme;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthState;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthenticationException;
import top.mrxiaom.pluginbase.resolver.http.auth.ContextAwareAuthScheme;
import top.mrxiaom.pluginbase.resolver.http.auth.Credentials;
import top.mrxiaom.pluginbase.resolver.http.auth.MalformedChallengeException;
import top.mrxiaom.pluginbase.resolver.http.client.AuthenticationStrategy;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.util.Asserts;

/**
 * @since 4.3
 */
public class HttpAuthenticator {

    public HttpAuthenticator() {
        super();
    }

    public boolean isAuthenticationRequested(
            final HttpHost host,
            final HttpResponse response,
            final AuthenticationStrategy authStrategy,
            final AuthState authState,
            final HttpContext context) {
        if (authStrategy.isAuthenticationRequested(host, response, context)) {
            if (authState.getState() == AuthProtocolState.SUCCESS) {
                authStrategy.authFailed(host, authState.getAuthScheme(), context);
            }
            return true;
        }
        switch (authState.getState()) {
        case CHALLENGED:
        case HANDSHAKE:
            authState.setState(AuthProtocolState.SUCCESS);
            authStrategy.authSucceeded(host, authState.getAuthScheme(), context);
            break;
        case SUCCESS:
            break;
        default:
            authState.setState(AuthProtocolState.UNCHALLENGED);
        }
        return false;
    }

    public boolean handleAuthChallenge(
            final HttpHost host,
            final HttpResponse response,
            final AuthenticationStrategy authStrategy,
            final AuthState authState,
            final HttpContext context) {
        try {
            final Map<String, Header> challenges = authStrategy.getChallenges(host, response, context);
            if (challenges.isEmpty()) {
                return false;
            }

            final AuthScheme authScheme = authState.getAuthScheme();
            switch (authState.getState()) {
            case FAILURE:
                return false;
            case SUCCESS:
                authState.reset();
                break;
            case CHALLENGED:
            case HANDSHAKE:
                if (authScheme == null) {
                    authStrategy.authFailed(host, null, context);
                    authState.reset();
                    authState.setState(AuthProtocolState.FAILURE);
                    return false;
                }
            case UNCHALLENGED:
                if (authScheme != null) {
                    final String id = authScheme.getSchemeName();
                    final Header challenge = challenges.get(id.toLowerCase(Locale.ROOT));
                    if (challenge != null) {
                        authScheme.processChallenge(challenge);
                        if (authScheme.isComplete()) {
                            authStrategy.authFailed(host, authState.getAuthScheme(), context);
                            authState.reset();
                            authState.setState(AuthProtocolState.FAILURE);
                            return false;
                        }
                        authState.setState(AuthProtocolState.HANDSHAKE);
                        return true;
                    }
                    authState.reset();
                    // Retry authentication with a different scheme
                }
            }
            final Queue<AuthOption> authOptions = authStrategy.select(challenges, host, response, context);
            if (authOptions != null && !authOptions.isEmpty()) {
                authState.setState(AuthProtocolState.CHALLENGED);
                authState.update(authOptions);
                return true;
            }
            return false;
        } catch (final MalformedChallengeException ex) {
            authState.reset();
            return false;
        }
    }

    public void generateAuthResponse(
            final HttpRequest request,
            final AuthState authState,
            final HttpContext context) {
        AuthScheme authScheme = authState.getAuthScheme();
        Credentials creds = authState.getCredentials();
        switch (authState.getState()) { // TODO add UNCHALLENGED and HANDSHAKE cases
        case FAILURE:
            return;
        case SUCCESS:
            ensureAuthScheme(authScheme);
            if (authScheme.isConnectionBased()) {
                return;
            }
            break;
        case CHALLENGED:
            final Queue<AuthOption> authOptions = authState.getAuthOptions();
            if (authOptions != null) {
                while (!authOptions.isEmpty()) {
                    final AuthOption authOption = authOptions.remove();
                    authScheme = authOption.getAuthScheme();
                    creds = authOption.getCredentials();
                    authState.update(authScheme, creds);
                    try {
                        final Header header = doAuth(authScheme, creds, request, context);
                        request.addHeader(header);
                        break;
                    } catch (final AuthenticationException ignored) {
                    }
                }
                return;
            }
            ensureAuthScheme(authScheme);
        }
        if (authScheme != null) {
            try {
                final Header header = doAuth(authScheme, creds, request, context);
                request.addHeader(header);
            } catch (final AuthenticationException ignored) {
            }
        }
    }

    private void ensureAuthScheme(final AuthScheme authScheme) {
        Asserts.notNull(authScheme, "Auth scheme");
    }

    @SuppressWarnings("deprecation")
    private Header doAuth(
            final AuthScheme authScheme,
            final Credentials creds,
            final HttpRequest request,
            final HttpContext context) throws AuthenticationException {
        return authScheme instanceof ContextAwareAuthScheme
                        ? ((ContextAwareAuthScheme) authScheme).authenticate(creds, request,
                                        context)
                        : authScheme.authenticate(creds, request);
    }

}
