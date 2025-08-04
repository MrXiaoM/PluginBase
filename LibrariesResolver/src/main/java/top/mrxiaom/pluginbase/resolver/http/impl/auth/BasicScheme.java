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

import java.nio.charset.Charset;

import top.mrxiaom.pluginbase.resolver.http.Consts;
import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.auth.AUTH;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthenticationException;
import top.mrxiaom.pluginbase.resolver.http.auth.Credentials;
import top.mrxiaom.pluginbase.resolver.http.auth.MalformedChallengeException;
import top.mrxiaom.pluginbase.resolver.http.message.BufferedHeader;
import top.mrxiaom.pluginbase.resolver.http.protocol.BasicHttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.util.Args;
import top.mrxiaom.pluginbase.resolver.http.util.CharArrayBuffer;
import top.mrxiaom.pluginbase.resolver.http.util.EncodingUtils;
import top.mrxiaom.pluginbase.resolver.http.util.codec.binary.Base64;

/**
 * Basic authentication scheme as defined in RFC 2617.
 *
 * @since 4.0
 */
public class BasicScheme extends RFC2617Scheme {

    private static final long serialVersionUID = -1931571557597830536L;

    /** Whether the basic authentication process is complete */
    private boolean complete;

    /**
     * @since 4.3
     */
    public BasicScheme(final Charset credentialsCharset) {
        super(credentialsCharset);
        this.complete = false;
    }

    public BasicScheme() {
        this(Consts.ASCII);
    }

    /**
     * Returns textual designation of the basic authentication scheme.
     *
     * @return {@code basic}
     */
    @Override
    public String getSchemeName() {
        return "basic";
    }

    /**
     * Processes the Basic challenge.
     *
     * @param header the challenge header
     *
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     */
    @Override
    public void processChallenge(
            final Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        this.complete = true;
    }

    /**
     * Tests if the Basic authentication process has been completed.
     *
     * @return {@code true} if Basic authorization has been processed,
     *   {@code false} otherwise.
     */
    @Override
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Returns {@code false}. Basic authentication scheme is request based.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isConnectionBased() {
        return false;
    }

    /**
     * @deprecated (4.2) Use {@link top.mrxiaom.pluginbase.resolver.http.auth.ContextAwareAuthScheme#authenticate(
     *   Credentials, HttpRequest, top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext)}
     */
    @Override
    @Deprecated
    public Header authenticate(
            final Credentials credentials, final HttpRequest request) throws AuthenticationException {
        return authenticate(credentials, request, new BasicHttpContext());
    }

    /**
     * Produces basic authorization header for the given set of {@link Credentials}.
     *
     * @param credentials The set of credentials to be used for authentication
     * @param request The request being authenticated
     * @return a basic authorization string
     */
    @Override
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request,
            final HttpContext context) {

        Args.notNull(credentials, "Credentials");
        Args.notNull(request, "HTTP request");
        String tmp = credentials.getUserPrincipal().getName() +
                ":" +
                ((credentials.getPassword() == null) ? "null" : credentials.getPassword());

        final Base64 base64codec = new Base64(0);
        final byte[] base64password = base64codec.encode(
                EncodingUtils.getBytes(tmp, getCredentialsCharset(request)));

        final CharArrayBuffer buffer = new CharArrayBuffer(32);
        if (isProxy()) {
            buffer.append(AUTH.PROXY_AUTH_RESP);
        } else {
            buffer.append(AUTH.WWW_AUTH_RESP);
        }
        buffer.append(": Basic ");
        buffer.append(base64password, 0, base64password.length);

        return new BufferedHeader(buffer);
    }

    @Override
    public String toString() {
        return "BASIC [complete=" + complete +
                "]";
    }
}
