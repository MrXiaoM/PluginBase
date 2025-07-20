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

package top.mrxiaom.pluginbase.resolver.http.impl.client;

import java.io.IOException;
import java.net.Socket;

import top.mrxiaom.pluginbase.resolver.http.ConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.HttpEntity;
import top.mrxiaom.pluginbase.resolver.http.HttpException;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.HttpResponse;
import top.mrxiaom.pluginbase.resolver.http.HttpVersion;
import top.mrxiaom.pluginbase.resolver.http.auth.AUTH;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthScope;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthState;
import top.mrxiaom.pluginbase.resolver.http.auth.Credentials;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.client.params.HttpClientParamConfig;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.HttpClientContext;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestClientConnControl;
import top.mrxiaom.pluginbase.resolver.http.config.ConnectionConfig;
import top.mrxiaom.pluginbase.resolver.http.conn.HttpConnectionFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.ManagedHttpClientConnection;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.RouteInfo.LayerType;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.RouteInfo.TunnelType;
import top.mrxiaom.pluginbase.resolver.http.entity.BufferedHttpEntity;
import top.mrxiaom.pluginbase.resolver.http.impl.DefaultConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.impl.auth.HttpAuthenticator;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.ManagedHttpClientConnectionFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.TunnelRefusedException;
import top.mrxiaom.pluginbase.resolver.http.message.BasicHttpRequest;
import top.mrxiaom.pluginbase.resolver.http.params.BasicHttpParams;
import top.mrxiaom.pluginbase.resolver.http.params.HttpParamConfig;
import top.mrxiaom.pluginbase.resolver.http.params.HttpParams;
import top.mrxiaom.pluginbase.resolver.http.protocol.BasicHttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpCoreContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpRequestExecutor;
import top.mrxiaom.pluginbase.resolver.http.protocol.ImmutableHttpProcessor;
import top.mrxiaom.pluginbase.resolver.http.protocol.RequestTargetHost;
import top.mrxiaom.pluginbase.resolver.http.protocol.RequestUserAgent;
import top.mrxiaom.pluginbase.resolver.http.util.Args;
import top.mrxiaom.pluginbase.resolver.http.util.EntityUtils;

/**
 * ProxyClient can be used to establish a tunnel via an HTTP proxy.
 */
public class ProxyClient {

    private final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory;
    private final ConnectionConfig connectionConfig;
    private final RequestConfig requestConfig;
    private final HttpProcessor httpProcessor;
    private final HttpRequestExecutor requestExec;
    private final ProxyAuthenticationStrategy proxyAuthStrategy;
    private final HttpAuthenticator authenticator;
    private final AuthState proxyAuthState;
    private final ConnectionReuseStrategy reuseStrategy;

    /**
     * @since 4.3
     */
    public ProxyClient(
            final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory,
            final ConnectionConfig connectionConfig,
            final RequestConfig requestConfig) {
        super();
        this.connFactory = connFactory != null ? connFactory : ManagedHttpClientConnectionFactory.INSTANCE;
        this.connectionConfig = connectionConfig != null ? connectionConfig : ConnectionConfig.DEFAULT;
        this.requestConfig = requestConfig != null ? requestConfig : RequestConfig.DEFAULT;
        this.httpProcessor = new ImmutableHttpProcessor(
                new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent());
        this.requestExec = new HttpRequestExecutor();
        this.proxyAuthStrategy = new ProxyAuthenticationStrategy();
        this.authenticator = new HttpAuthenticator();
        this.proxyAuthState = new AuthState();
        this.reuseStrategy = new DefaultConnectionReuseStrategy();
    }

    /**
     * @deprecated (4.3) use {@link ProxyClient#ProxyClient(HttpConnectionFactory, ConnectionConfig, RequestConfig)}
     */
    @Deprecated
    public ProxyClient(final HttpParams params) {
        this(null,
                HttpParamConfig.getConnectionConfig(params),
                HttpClientParamConfig.getRequestConfig(params));
    }

    /**
     * @since 4.3
     */
    public ProxyClient(final RequestConfig requestConfig) {
        this(null, null, requestConfig);
    }

    public ProxyClient() {
        this(null, null, null);
    }

    /**
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public HttpParams getParams() {
        return new BasicHttpParams();
    }

    public Socket tunnel(
            final HttpHost proxy,
            final HttpHost target,
            final Credentials credentials) throws IOException, HttpException {
        Args.notNull(proxy, "Proxy host");
        Args.notNull(target, "Target host");
        Args.notNull(credentials, "Credentials");
        HttpHost host = target;
        if (host.getPort() <= 0) {
            host = new HttpHost(host.getHostName(), 80, host.getSchemeName());
        }
        final HttpRoute route = new HttpRoute(
                host,
                this.requestConfig.getLocalAddress(),
                proxy, false, TunnelType.TUNNELLED, LayerType.PLAIN);

        final ManagedHttpClientConnection conn = this.connFactory.create(
                route, this.connectionConfig);
        final HttpContext context = new BasicHttpContext();
        HttpResponse response;

        final HttpRequest connect = new BasicHttpRequest(
                "CONNECT", host.toHostString(), HttpVersion.HTTP_1_1);

        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxy), credentials);

        // Populate the execution context
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, target);
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, conn);
        context.setAttribute(HttpCoreContext.HTTP_REQUEST, connect);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.PROXY_AUTH_STATE, this.proxyAuthState);
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, this.requestConfig);

        this.requestExec.preProcess(connect, this.httpProcessor, context);

        for (;;) {
            if (!conn.isOpen()) {
                final Socket socket = new Socket(proxy.getHostName(), proxy.getPort());
                conn.bind(socket);
            }

            this.authenticator.generateAuthResponse(connect, this.proxyAuthState, context);

            response = this.requestExec.execute(connect, conn, context);

            final int status = response.getStatusLine().getStatusCode();
            if (status < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " +
                        response.getStatusLine());
            }
            if (this.authenticator.isAuthenticationRequested(proxy, response,
                    this.proxyAuthStrategy, this.proxyAuthState, context)) {
                if (this.authenticator.handleAuthChallenge(proxy, response,
                        this.proxyAuthStrategy, this.proxyAuthState, context)) {
                    // Retry request
                    if (this.reuseStrategy.keepAlive(response, context)) {
                        // Consume response content
                        final HttpEntity entity = response.getEntity();
                        EntityUtils.consume(entity);
                    } else {
                        conn.close();
                    }
                    // discard previous auth header
                    connect.removeHeaders(AUTH.PROXY_AUTH_RESP);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        final int status = response.getStatusLine().getStatusCode();

        if (status > 299) {

            // Buffer response content
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                response.setEntity(new BufferedHttpEntity(entity));
            }

            conn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " +
                    response.getStatusLine(), response);
        }
        return conn.getSocket();
    }

}
