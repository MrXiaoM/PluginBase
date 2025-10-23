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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import top.mrxiaom.pluginbase.resolver.http.ConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.client.HttpRequestRetryHandler;
import top.mrxiaom.pluginbase.resolver.http.client.ServiceUnavailableRetryStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.UserTokenHandler;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestClientConnControl;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestExpectContinue;
import top.mrxiaom.pluginbase.resolver.http.config.RegistryBuilder;
import top.mrxiaom.pluginbase.resolver.http.config.SocketConfig;
import top.mrxiaom.pluginbase.resolver.http.conn.ConnectionKeepAliveStrategy;
import top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager;
import top.mrxiaom.pluginbase.resolver.http.conn.SchemePortResolver;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.conn.socket.ConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.socket.LayeredConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.socket.PlainConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.ssl.DefaultHostnameVerifier;
import top.mrxiaom.pluginbase.resolver.http.conn.ssl.SSLConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.util.PublicSuffixMatcher;
import top.mrxiaom.pluginbase.resolver.http.conn.util.PublicSuffixMatcherLoader;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.DefaultProxyRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.DefaultRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.DefaultSchemePortResolver;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.PoolingHttpClientConnectionManager;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.ClientExecChain;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.MainClientExec;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.ProtocolExec;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.RedirectExec;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.RetryExec;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.ServiceUnavailableRetryExec;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessorBuilder;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpRequestExecutor;
import top.mrxiaom.pluginbase.resolver.http.protocol.ImmutableHttpProcessor;
import top.mrxiaom.pluginbase.resolver.http.protocol.RequestContent;
import top.mrxiaom.pluginbase.resolver.http.protocol.RequestTargetHost;
import top.mrxiaom.pluginbase.resolver.http.protocol.RequestUserAgent;
import top.mrxiaom.pluginbase.resolver.http.ssl.SSLContexts;
import top.mrxiaom.pluginbase.resolver.http.util.VersionInfo;

/**
 * Builder for {@link CloseableHttpClient} instances.
 * <p>
 * When a particular component is not explicitly set this class will
 * use its default implementation.
 * </p>
 * <ul>
 *  <li>ssl.TrustManagerFactory.algorithm</li>
 *  <li>javax.net.ssl.trustStoreType</li>
 *  <li>javax.net.ssl.trustStore</li>
 *  <li>javax.net.ssl.trustStoreProvider</li>
 *  <li>javax.net.ssl.trustStorePassword</li>
 *  <li>ssl.KeyManagerFactory.algorithm</li>
 *  <li>javax.net.ssl.keyStoreType</li>
 *  <li>javax.net.ssl.keyStore</li>
 *  <li>javax.net.ssl.keyStoreProvider</li>
 *  <li>javax.net.ssl.keyStorePassword</li>
 *  <li>https.protocols</li>
 *  <li>https.cipherSuites</li>
 *  <li>http.proxyHost</li>
 *  <li>http.proxyPort</li>
 *  <li>https.proxyHost</li>
 *  <li>https.proxyPort</li>
 *  <li>http.nonProxyHosts</li>
 *  <li>https.proxyUser</li>
 *  <li>http.proxyUser</li>
 *  <li>https.proxyPassword</li>
 *  <li>http.proxyPassword</li>
 *  <li>http.keepAlive</li>
 *  <li>http.maxConnections</li>
 *  <li>http.agent</li>
 * </ul>
 * <p>
 * Please note that some settings used by this class can be mutually
 * exclusive and may not apply when building {@link CloseableHttpClient}
 * instances.
 * </p>
 *
 * @since 4.3
 */
public class HttpClientBuilder {

    private HttpClientConnectionManager connManager;
    private boolean connManagerShared;
    private ConnectionReuseStrategy reuseStrategy;

    private HttpRequestRetryHandler retryHandler;
    private ServiceUnavailableRetryStrategy serviceUnavailStrategy;
    private String userAgent;
    private HttpHost proxy;
    private SocketConfig defaultSocketConfig;
    private RequestConfig defaultRequestConfig;

    private int maxConnTotal = 0;
    private int maxConnPerRoute = 0;

    private long connTimeToLive = -1;
    private TimeUnit connTimeToLiveTimeUnit = TimeUnit.MILLISECONDS;

    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    protected HttpClientBuilder() {
        super();
    }

    /**
     * Assigns default {@link SocketConfig}.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} method.
     * </p>
     */
    public final HttpClientBuilder setDefaultSocketConfig(final SocketConfig config) {
        this.defaultSocketConfig = config;
        return this;
    }

    /**
     * Assigns {@link HttpClientConnectionManager} instance.
     */
    public final HttpClientBuilder setConnectionManager(
            final HttpClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    /**
     * Defines the connection manager is to be shared by multiple
     * client instances.
     * <p>
     * If the connection manager is shared its life-cycle is expected
     * to be managed by the caller and it will not be shut down
     * if the client is closed.
     * </p>
     *
     * @param shared defines whether or not the connection manager can be shared
     *  by multiple clients.
     *
     * @since 4.4
     */
    public final HttpClientBuilder setConnectionManagerShared(
            final boolean shared) {
        this.connManagerShared = shared;
        return this;
    }

    /**
     * Assigns {@link ConnectionReuseStrategy} instance.
     */
    public final HttpClientBuilder setConnectionReuseStrategy(
            final ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
        return this;
    }

    /**
     * Assigns {@code User-Agent} value.
     */
    public final HttpClientBuilder setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Assigns {@link HttpRequestRetryHandler} instance.
     */
    public final HttpClientBuilder setRetryHandler(final HttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
        return this;
    }

    /**
     * Assigns default proxy value.
     */
    public final HttpClientBuilder setProxy(final HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Assigns {@link ServiceUnavailableRetryStrategy} instance.
     */
    public final HttpClientBuilder setServiceUnavailableRetryStrategy(
            final ServiceUnavailableRetryStrategy serviceUnavailStrategy) {
        this.serviceUnavailStrategy = serviceUnavailStrategy;
        return this;
    }

    /**
     * Assigns default {@link RequestConfig} instance which will be used
     * for request execution if not explicitly set in the client execution
     * context.
     */
    public final HttpClientBuilder setDefaultRequestConfig(final RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    /**
     * Produces an instance of {@link ClientExecChain} to be used as a main exec.
     * <p>
     * Default implementation produces an instance of {@link MainClientExec}
     * </p>
     * <p>
     * For internal use.
     * </p>
     *
     * @since 4.4
     */
    protected ClientExecChain createMainExec(
            final HttpRequestExecutor requestExec,
            final HttpClientConnectionManager connManager,
            final ConnectionReuseStrategy reuseStrategy,
            final ConnectionKeepAliveStrategy keepAliveStrategy,
            final HttpProcessor proxyHttpProcessor,
            final UserTokenHandler userTokenHandler)
    {
        return new MainClientExec(
                requestExec,
                connManager,
                reuseStrategy,
                keepAliveStrategy,
                proxyHttpProcessor,
                userTokenHandler);
    }

    /**
     * For internal use.
     */
    protected ClientExecChain decorateMainExec(final ClientExecChain mainExec) {
        return mainExec;
    }

    /**
     * For internal use.
     */
    protected ClientExecChain decorateProtocolExec(final ClientExecChain protocolExec) {
        return protocolExec;
    }

    public CloseableHttpClient build() {
        // Create main request executor
        // We copy the instance fields to avoid changing them, and rename to avoid accidental use of the wrong version
        PublicSuffixMatcher publicSuffixMatcherCopy = PublicSuffixMatcherLoader.getDefault();

        HttpRequestExecutor requestExecCopy = new HttpRequestExecutor();
        HttpClientConnectionManager connManagerCopy = this.connManager;
        if (connManagerCopy == null) {
            HostnameVerifier hostnameVerifierCopy = new DefaultHostnameVerifier(publicSuffixMatcherCopy);
            LayeredConnectionSocketFactory sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                        SSLContexts.createDefault(),
                        hostnameVerifierCopy);

            @SuppressWarnings("resource")
            final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactoryCopy)
                        .build(),
                    null,
                    null,
                    connTimeToLive,
                    connTimeToLiveTimeUnit != null ? connTimeToLiveTimeUnit : TimeUnit.MILLISECONDS);
            if (defaultSocketConfig != null) {
                poolingmgr.setDefaultSocketConfig(defaultSocketConfig);
            }
            if (maxConnTotal > 0) {
                poolingmgr.setMaxTotal(maxConnTotal);
            }
            if (maxConnPerRoute > 0) {
                poolingmgr.setDefaultMaxPerRoute(maxConnPerRoute);
            }
            connManagerCopy = poolingmgr;
        }
        ConnectionReuseStrategy reuseStrategyCopy = this.reuseStrategy;
        if (reuseStrategyCopy == null) {
            reuseStrategyCopy = DefaultClientConnectionReuseStrategy.INSTANCE;
        }
        ConnectionKeepAliveStrategy keepAliveStrategyCopy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        UserTokenHandler userTokenHandlerCopy = DefaultUserTokenHandler.INSTANCE;

        String userAgentCopy = this.userAgent;
        if (userAgentCopy == null) {
            userAgentCopy = VersionInfo.getUserAgent("Apache-HttpClient",
                    "top.mrxiaom.pluginbase.resolver.http.client", getClass());
        }

        ClientExecChain execChain = createMainExec(
                requestExecCopy,
                connManagerCopy,
                reuseStrategyCopy,
                keepAliveStrategyCopy,
                new ImmutableHttpProcessor(new RequestTargetHost(), new RequestUserAgent(userAgentCopy)),
                userTokenHandlerCopy);

        execChain = decorateMainExec(execChain);

        final HttpProcessorBuilder b = HttpProcessorBuilder.create();
        b.addAll(
                new RequestContent(),
                new RequestTargetHost(),
                new RequestClientConnControl(),
                new RequestUserAgent(userAgentCopy),
                new RequestExpectContinue());
        HttpProcessor httpprocessorCopy = b.build();
        execChain = new ProtocolExec(execChain, httpprocessorCopy);

        execChain = decorateProtocolExec(execChain);

        // Add request retry executor, if not disabled
        HttpRequestRetryHandler retryHandlerCopy = this.retryHandler;
        if (retryHandlerCopy == null) {
            retryHandlerCopy = DefaultHttpRequestRetryHandler.INSTANCE;
        }
        execChain = new RetryExec(execChain, retryHandlerCopy);


        HttpRoutePlanner routePlannerCopy;
        SchemePortResolver schemePortResolverCopy = DefaultSchemePortResolver.INSTANCE;
        if (proxy != null) {
            routePlannerCopy = new DefaultProxyRoutePlanner(proxy, schemePortResolverCopy);
        } else {
            routePlannerCopy = new DefaultRoutePlanner(schemePortResolverCopy);
        }

        // Optionally, add service unavailable retry executor
        final ServiceUnavailableRetryStrategy serviceUnavailStrategyCopy = this.serviceUnavailStrategy;
        if (serviceUnavailStrategyCopy != null) {
            execChain = new ServiceUnavailableRetryExec(execChain, serviceUnavailStrategyCopy);
        }

        // Add redirect executor
        execChain = new RedirectExec(execChain, routePlannerCopy, DefaultRedirectStrategy.INSTANCE);

        List<Closeable> closeablesCopy = new ArrayList<>();
        if (!this.connManagerShared) {
            closeablesCopy.add(connManagerCopy::shutdown);
        }

        return new InternalHttpClient(
                execChain,
                connManagerCopy,
                routePlannerCopy,
                defaultRequestConfig != null ? defaultRequestConfig : RequestConfig.DEFAULT,
                closeablesCopy);
    }

}
