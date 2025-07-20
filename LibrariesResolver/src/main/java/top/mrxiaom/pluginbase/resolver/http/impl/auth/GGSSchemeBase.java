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

import java.net.InetAddress;
import java.net.UnknownHostException;

import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.auth.AUTH;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthenticationException;
import top.mrxiaom.pluginbase.resolver.http.auth.Credentials;
import top.mrxiaom.pluginbase.resolver.http.auth.InvalidCredentialsException;
import top.mrxiaom.pluginbase.resolver.http.auth.KerberosCredentials;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.HttpClientContext;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.message.BufferedHeader;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.util.Args;
import top.mrxiaom.pluginbase.resolver.http.util.CharArrayBuffer;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import top.mrxiaom.pluginbase.resolver.http.util.codec.binary.Base64;

/**
 * @since 4.2
 */
public abstract class GGSSchemeBase extends AuthSchemeBase {

    enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        TOKEN_GENERATED,
        FAILED,
    }

    private final Base64 base64codec;
    private final boolean stripPort;
    private final boolean useCanonicalHostname;

    /** Authentication process state */
    private State state;

    /** base64 decoded challenge **/
    private byte[] token;

    GGSSchemeBase(final boolean stripPort, final boolean useCanonicalHostname) {
        super();
        this.base64codec = new Base64(0);
        this.stripPort = stripPort;
        this.useCanonicalHostname = useCanonicalHostname;
        this.state = State.UNINITIATED;
    }

    GGSSchemeBase(final boolean stripPort) {
        this(stripPort, true);
    }

    GGSSchemeBase() {
        this(true,true);
    }

    protected GSSManager getManager() {
        return GSSManager.getInstance();
    }

    /**
     * @since 4.4
     */
    protected byte[] generateGSSToken(
            final byte[] input, final Oid oid, final String authServer,
            final Credentials credentials) throws GSSException {
        final GSSManager manager = getManager();
        final GSSName serverName = manager.createName("HTTP@" + authServer, GSSName.NT_HOSTBASED_SERVICE);

        final GSSCredential gssCredential;
        if (credentials instanceof KerberosCredentials) {
            gssCredential = ((KerberosCredentials) credentials).getGSSCredential();
        } else {
            gssCredential = null;
        }

        final GSSContext gssContext = createGSSContext(manager, oid, serverName, gssCredential);
        return input != null
                        ? gssContext.initSecContext(input, 0, input.length)
                        : gssContext.initSecContext(new byte[] {}, 0, 0);
    }

    GSSContext createGSSContext(
            final GSSManager manager,
            final Oid oid,
            final GSSName serverName,
            final GSSCredential gssCredential) throws GSSException {
        final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, gssCredential,
                GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        return gssContext;
    }
    /**
     * @deprecated (4.4) Use {@link #generateToken(byte[], String, top.mrxiaom.pluginbase.resolver.http.auth.Credentials)}.
     */
    @Deprecated
    protected byte[] generateToken(final byte[] input, final String authServer) throws GSSException {
        return null;
    }

    /**
     * @since 4.4
     */
    //TODO: make this method abstract
    @SuppressWarnings("deprecation")
    protected byte[] generateToken(
            final byte[] input, final String authServer, final Credentials credentials) throws GSSException {
        return generateToken(input, authServer);
    }

    @Override
    public boolean isComplete() {
        return this.state == State.TOKEN_GENERATED || this.state == State.FAILED;
    }

    /**
     * @deprecated (4.2) Use {@link top.mrxiaom.pluginbase.resolver.http.auth.ContextAwareAuthScheme#authenticate(
     *   Credentials, HttpRequest, top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext)}
     */
    @Override
    @Deprecated
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request) throws AuthenticationException {
        return authenticate(credentials, request, null);
    }

    @Override
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request,
            final HttpContext context) throws AuthenticationException {
        Args.notNull(request, "HTTP request");
        switch (state) {
        case UNINITIATED:
            throw new AuthenticationException(getSchemeName() + " authentication has not been initiated");
        case FAILED:
            throw new AuthenticationException(getSchemeName() + " authentication has failed");
        case CHALLENGE_RECEIVED:
            try {
                final HttpRoute route = (HttpRoute) context.getAttribute(HttpClientContext.HTTP_ROUTE);
                if (route == null) {
                    throw new AuthenticationException("Connection route is not available");
                }
                HttpHost host;
                if (isProxy()) {
                    host = route.getProxyHost();
                    if (host == null) {
                        host = route.getTargetHost();
                    }
                } else {
                    host = route.getTargetHost();
                }
                final String authServer;
                String hostname = host.getHostName();

                if (this.useCanonicalHostname){
                    try {
                         //TODO: uncomment this statement and delete the resolveCanonicalHostname,
                         //TODO: as soon canonical hostname resolving is implemented in the SystemDefaultDnsResolver
                         //final DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
                         //hostname = dnsResolver.resolveCanonicalHostname(host.getHostName());
                         hostname = resolveCanonicalHostname(hostname);
                    } catch (final UnknownHostException ignore){
                    }
                }
                if (this.stripPort) { // || host.getPort()==80 || host.getPort()==443) {
                    authServer = hostname;
                } else {
                    authServer = hostname + ":" + host.getPort();
                }

                token = generateToken(token, authServer, credentials);
                state = State.TOKEN_GENERATED;
            } catch (final GSSException gsse) {
                state = State.FAILED;
                if (gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL
                        || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED) {
                    throw new InvalidCredentialsException(gsse.getMessage(), gsse);
                }
                if (gsse.getMajor() == GSSException.NO_CRED ) {
                    throw new InvalidCredentialsException(gsse.getMessage(), gsse);
                }
                if (gsse.getMajor() == GSSException.DEFECTIVE_TOKEN
                        || gsse.getMajor() == GSSException.DUPLICATE_TOKEN
                        || gsse.getMajor() == GSSException.OLD_TOKEN) {
                    throw new AuthenticationException(gsse.getMessage(), gsse);
                }
                // other error
                throw new AuthenticationException(gsse.getMessage());
            }
        case TOKEN_GENERATED:
            final String tokenstr = new String(base64codec.encode(token));

            final CharArrayBuffer buffer = new CharArrayBuffer(32);
            if (isProxy()) {
                buffer.append(AUTH.PROXY_AUTH_RESP);
            } else {
                buffer.append(AUTH.WWW_AUTH_RESP);
            }
            buffer.append(": Negotiate ");
            buffer.append(tokenstr);
            return new BufferedHeader(buffer);
        default:
            throw new IllegalStateException("Illegal state: " + state);
        }
    }

    @Override
    protected void parseChallenge(
            final CharArrayBuffer buffer,
            final int beginIndex, final int endIndex) {
        final String challenge = buffer.substringTrimmed(beginIndex, endIndex);

        if (state == State.UNINITIATED) {
            token = Base64.decodeBase64(challenge.getBytes());
            state = State.CHALLENGE_RECEIVED;
        } else {
            state = State.FAILED;
        }
    }

    private String resolveCanonicalHostname(final String host) throws UnknownHostException {
        final InetAddress in = InetAddress.getByName(host);
        final String canonicalServer = in.getCanonicalHostName();
        if (in.getHostAddress().contentEquals(canonicalServer)) {
            return host;
        }
        return canonicalServer;
    }

}
