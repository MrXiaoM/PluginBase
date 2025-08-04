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
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import top.mrxiaom.pluginbase.resolver.http.ConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpRequestInterceptor;
import top.mrxiaom.pluginbase.resolver.http.HttpResponseInterceptor;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthSchemeProvider;
import top.mrxiaom.pluginbase.resolver.http.client.AuthenticationStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.BackoffManager;
import top.mrxiaom.pluginbase.resolver.http.client.ConnectionBackoffStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.CredentialsProvider;
import top.mrxiaom.pluginbase.resolver.http.client.HttpRequestRetryHandler;
import top.mrxiaom.pluginbase.resolver.http.client.RedirectStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.ServiceUnavailableRetryStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.UserTokenHandler;
import top.mrxiaom.pluginbase.resolver.http.client.config.AuthSchemes;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.client.entity.InputStreamFactory;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestAcceptEncoding;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestAuthCache;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestClientConnControl;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestDefaultHeaders;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.RequestExpectContinue;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.ResponseContentEncoding;
import top.mrxiaom.pluginbase.resolver.http.config.ConnectionConfig;
import top.mrxiaom.pluginbase.resolver.http.config.Lookup;
import top.mrxiaom.pluginbase.resolver.http.config.RegistryBuilder;
import top.mrxiaom.pluginbase.resolver.http.config.SocketConfig;
import top.mrxiaom.pluginbase.resolver.http.conn.ConnectionKeepAliveStrategy;
import top.mrxiaom.pluginbase.resolver.http.conn.DnsResolver;
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
import top.mrxiaom.pluginbase.resolver.http.impl.NoConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.impl.auth.BasicSchemeFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.auth.DigestSchemeFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.auth.KerberosSchemeFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.auth.NTLMSchemeFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.auth.SPNegoSchemeFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.DefaultProxyRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.DefaultRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.DefaultSchemePortResolver;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.PoolingHttpClientConnectionManager;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.SystemDefaultRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.BackoffStrategyExec;
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
import top.mrxiaom.pluginbase.resolver.http.util.TextUtils;
import top.mrxiaom.pluginbase.resolver.http.util.VersionInfo;

/**
 * Builder for {@link CloseableHttpClient} instances.
 * <p>
 * When a particular component is not explicitly set this class will
 * use its default implementation. System properties will be taken
 * into account when configuring the default implementations when
 * {@link #useSystemProperties()} method is called prior to calling
 * {@link #build()}.
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

    private HttpRequestExecutor requestExec;
    private HostnameVerifier hostnameVerifier;
    private LayeredConnectionSocketFactory sslSocketFactory;
    private SSLContext sslContext;
    private HttpClientConnectionManager connManager;
    private boolean connManagerShared;
    private SchemePortResolver schemePortResolver;
    private ConnectionReuseStrategy reuseStrategy;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private AuthenticationStrategy targetAuthStrategy;
    private AuthenticationStrategy proxyAuthStrategy;
    private UserTokenHandler userTokenHandler;
    private HttpProcessor httpprocessor;
    private DnsResolver dnsResolver;

    private LinkedList<HttpRequestInterceptor> requestFirst;
    private LinkedList<HttpRequestInterceptor> requestLast;
    private LinkedList<HttpResponseInterceptor> responseFirst;
    private LinkedList<HttpResponseInterceptor> responseLast;

    private HttpRequestRetryHandler retryHandler;
    private HttpRoutePlanner routePlanner;
    private RedirectStrategy redirectStrategy;
    private ConnectionBackoffStrategy connectionBackoffStrategy;
    private BackoffManager backoffManager;
    private ServiceUnavailableRetryStrategy serviceUnavailStrategy;
    private Lookup<AuthSchemeProvider> authSchemeRegistry;
    private Map<String, InputStreamFactory> contentDecoderMap;
    private CredentialsProvider credentialsProvider;
    private String userAgent;
    private HttpHost proxy;
    private Collection<? extends Header> defaultHeaders;
    private SocketConfig defaultSocketConfig;
    private ConnectionConfig defaultConnectionConfig;
    private RequestConfig defaultRequestConfig;

    private boolean systemProperties;
    private boolean redirectHandlingDisabled;
    private boolean automaticRetriesDisabled;
    private boolean contentCompressionDisabled;
    private boolean authCachingDisabled;
    private boolean connectionStateDisabled;
    private boolean defaultUserAgentDisabled;

    private int maxConnTotal = 0;
    private int maxConnPerRoute = 0;

    private long connTimeToLive = -1;
    private TimeUnit connTimeToLiveTimeUnit = TimeUnit.MILLISECONDS;

    private List<Closeable> closeables;

    private PublicSuffixMatcher publicSuffixMatcher;

    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    protected HttpClientBuilder() {
        super();
    }

    /**
     * Assigns {@link HttpRequestExecutor} instance.
     */
    public final HttpClientBuilder setRequestExecutor(final HttpRequestExecutor requestExec) {
        this.requestExec = requestExec;
        return this;
    }

    /**
     * Assigns {@link javax.net.ssl.HostnameVerifier} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} and the {@link #setSSLSocketFactory(
     *   top.mrxiaom.pluginbase.resolver.http.conn.socket.LayeredConnectionSocketFactory)} methods.
     * </p>
     *
     *   @since 4.4
     */
    public final HttpClientBuilder setSSLHostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    /**
     * Assigns file containing public suffix matcher. Instances of this class can be created
     * with {@link top.mrxiaom.pluginbase.resolver.http.conn.util.PublicSuffixMatcherLoader}.
     *
     * @see top.mrxiaom.pluginbase.resolver.http.conn.util.PublicSuffixMatcher
     * @see top.mrxiaom.pluginbase.resolver.http.conn.util.PublicSuffixMatcherLoader
     *
     *   @since 4.4
     */
    public final HttpClientBuilder setPublicSuffixMatcher(final PublicSuffixMatcher publicSuffixMatcher) {
        this.publicSuffixMatcher = publicSuffixMatcher;
        return this;
    }

    /**
     * Assigns {@link SSLContext} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} and the {@link #setSSLSocketFactory(
     *   top.mrxiaom.pluginbase.resolver.http.conn.socket.LayeredConnectionSocketFactory)} methods.
     * </p>
     */
    public final HttpClientBuilder setSSLContext(final SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /**
     * Assigns {@link LayeredConnectionSocketFactory} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} method.
     * </p>
     */
    public final HttpClientBuilder setSSLSocketFactory(
            final LayeredConnectionSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    /**
     * Assigns maximum total connection value.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} method.
     * </p>
     */
    public final HttpClientBuilder setMaxConnTotal(final int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    /**
     * Assigns maximum connection per route value.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} method.
     * </p>
     */
    public final HttpClientBuilder setMaxConnPerRoute(final int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
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
     * Assigns default {@link ConnectionConfig}.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} method.
     * </p>
     */
    public final HttpClientBuilder setDefaultConnectionConfig(final ConnectionConfig config) {
        this.defaultConnectionConfig = config;
        return this;
    }

    /**
     * Sets maximum time to live for persistent connections
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager)} method.
     * </p>
     *
     * @since 4.4
     */
    public final HttpClientBuilder setConnectionTimeToLive(final long connTimeToLive, final TimeUnit connTimeToLiveTimeUnit) {
        this.connTimeToLive = connTimeToLive;
        this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
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
     * Assigns {@link ConnectionKeepAliveStrategy} instance.
     */
    public final HttpClientBuilder setKeepAliveStrategy(
            final ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
        return this;
    }

    /**
     * Assigns {@link AuthenticationStrategy} instance for target
     * host authentication.
     */
    public final HttpClientBuilder setTargetAuthenticationStrategy(
            final AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
        return this;
    }

    /**
     * Assigns {@link AuthenticationStrategy} instance for proxy
     * authentication.
     */
    public final HttpClientBuilder setProxyAuthenticationStrategy(
            final AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
        return this;
    }

    /**
     * Assigns {@link UserTokenHandler} instance.
     * <p>
     * Please note this value can be overridden by the {@link #disableConnectionState()}
     * method.
     * </p>
     */
    public final HttpClientBuilder setUserTokenHandler(final UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
        return this;
    }

    /**
     * Disables connection state tracking.
     */
    public final HttpClientBuilder disableConnectionState() {
        connectionStateDisabled = true;
        return this;
    }

    /**
     * Assigns {@link SchemePortResolver} instance.
     */
    public final HttpClientBuilder setSchemePortResolver(
            final SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    /**
     * Assigns {@code User-Agent} value.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     * </p>
     */
    public final HttpClientBuilder setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Assigns default request header values.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     * </p>
     */
    public final HttpClientBuilder setDefaultHeaders(final Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    /**
     * Adds this protocol interceptor to the head of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     * </p>
     */
    public final HttpClientBuilder addInterceptorFirst(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (responseFirst == null) {
            responseFirst = new LinkedList<>();
        }
        responseFirst.addFirst(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the tail of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     * </p>
     */
    public final HttpClientBuilder addInterceptorLast(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (responseLast == null) {
            responseLast = new LinkedList<>();
        }
        responseLast.addLast(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the head of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     */
    public final HttpClientBuilder addInterceptorFirst(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (requestFirst == null) {
            requestFirst = new LinkedList<>();
        }
        requestFirst.addFirst(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the tail of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     */
    public final HttpClientBuilder addInterceptorLast(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (requestLast == null) {
            requestLast = new LinkedList<>();
        }
        requestLast.addLast(itcp);
        return this;
    }

    /**
     * Disables automatic content decompression.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     */
    public final HttpClientBuilder disableContentCompression() {
        contentCompressionDisabled = true;
        return this;
    }

    /**
     * Disables authentication scheme caching.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * top.mrxiaom.pluginbase.resolver.http.protocol.HttpProcessor)} method.
     */
    public final HttpClientBuilder disableAuthCaching() {
        this.authCachingDisabled = true;
        return this;
    }

    /**
     * Assigns {@link HttpProcessor} instance.
     */
    public final HttpClientBuilder setHttpProcessor(final HttpProcessor httpprocessor) {
        this.httpprocessor = httpprocessor;
        return this;
    }

    /**
     * Assigns {@link DnsResolver} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(HttpClientConnectionManager)} method.
     */
    public final HttpClientBuilder setDnsResolver(final DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;
        return this;
    }

    /**
     * Assigns {@link HttpRequestRetryHandler} instance.
     * <p>
     * Please note this value can be overridden by the {@link #disableAutomaticRetries()}
     * method.
     */
    public final HttpClientBuilder setRetryHandler(final HttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
        return this;
    }

    /**
     * Disables automatic request recovery and re-execution.
     */
    public final HttpClientBuilder disableAutomaticRetries() {
        automaticRetriesDisabled = true;
        return this;
    }

    /**
     * Assigns default proxy value.
     * <p>
     * Please note this value can be overridden by the {@link #setRoutePlanner(
     *   top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoutePlanner)} method.
     */
    public final HttpClientBuilder setProxy(final HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Assigns {@link HttpRoutePlanner} instance.
     */
    public final HttpClientBuilder setRoutePlanner(final HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    /**
     * Assigns {@link RedirectStrategy} instance.
     * <p>
     * Please note this value can be overridden by the {@link #disableRedirectHandling()}
     * method.
     * </p>
`     */
    public final HttpClientBuilder setRedirectStrategy(final RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    /**
     * Disables automatic redirect handling.
     */
    public final HttpClientBuilder disableRedirectHandling() {
        redirectHandlingDisabled = true;
        return this;
    }

    /**
     * Assigns {@link ConnectionBackoffStrategy} instance.
     */
    public final HttpClientBuilder setConnectionBackoffStrategy(
            final ConnectionBackoffStrategy connectionBackoffStrategy) {
        this.connectionBackoffStrategy = connectionBackoffStrategy;
        return this;
    }

    /**
     * Assigns {@link BackoffManager} instance.
     */
    public final HttpClientBuilder setBackoffManager(final BackoffManager backoffManager) {
        this.backoffManager = backoffManager;
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
     * Assigns default {@link CredentialsProvider} instance which will be used
     * for request execution if not explicitly set in the client execution
     * context.
     */
    public final HttpClientBuilder setDefaultCredentialsProvider(
            final CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    /**
     * Assigns default {@link top.mrxiaom.pluginbase.resolver.http.auth.AuthScheme} registry which will
     * be used for request execution if not explicitly set in the client execution
     * context.
     */
    public final HttpClientBuilder setDefaultAuthSchemeRegistry(
            final Lookup<AuthSchemeProvider> authSchemeRegistry) {
        this.authSchemeRegistry = authSchemeRegistry;
        return this;
    }

    /**
     * Assigns a map of {@link top.mrxiaom.pluginbase.resolver.http.client.entity.InputStreamFactory}s
     * to be used for automatic content decompression.
     */
    public final HttpClientBuilder setContentDecoderRegistry(
            final Map<String, InputStreamFactory> contentDecoderMap) {
        this.contentDecoderMap = contentDecoderMap;
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
     * Use system properties when creating and configuring default
     * implementations.
     */
    public final HttpClientBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    /**
     * Disables the default user agent set by this builder if none has been provided by the user.
     *
     * @since 4.5.7
     */
    public final HttpClientBuilder disableDefaultUserAgent() {
        this.defaultUserAgentDisabled = true;
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
            final AuthenticationStrategy targetAuthStrategy,
            final AuthenticationStrategy proxyAuthStrategy,
            final UserTokenHandler userTokenHandler)
    {
        return new MainClientExec(
                requestExec,
                connManager,
                reuseStrategy,
                keepAliveStrategy,
                proxyHttpProcessor,
                targetAuthStrategy,
                proxyAuthStrategy,
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

    /**
     * For internal use.
     */
    protected void addCloseable(final Closeable closeable) {
        if (closeable == null) {
            return;
        }
        if (closeables == null) {
            closeables = new ArrayList<>();
        }
        closeables.add(closeable);
    }

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public CloseableHttpClient build() {
        // Create main request executor
        // We copy the instance fields to avoid changing them, and rename to avoid accidental use of the wrong version
        PublicSuffixMatcher publicSuffixMatcherCopy = this.publicSuffixMatcher;
        if (publicSuffixMatcherCopy == null) {
            publicSuffixMatcherCopy = PublicSuffixMatcherLoader.getDefault();
        }

        HttpRequestExecutor requestExecCopy = this.requestExec;
        if (requestExecCopy == null) {
            requestExecCopy = new HttpRequestExecutor();
        }
        HttpClientConnectionManager connManagerCopy = this.connManager;
        if (connManagerCopy == null) {
            LayeredConnectionSocketFactory sslSocketFactoryCopy = this.sslSocketFactory;
            if (sslSocketFactoryCopy == null) {
                final String[] supportedProtocols = systemProperties ? split(
                        System.getProperty("https.protocols")) : null;
                final String[] supportedCipherSuites = systemProperties ? split(
                        System.getProperty("https.cipherSuites")) : null;
                HostnameVerifier hostnameVerifierCopy = this.hostnameVerifier;
                if (hostnameVerifierCopy == null) {
                    hostnameVerifierCopy = new DefaultHostnameVerifier(publicSuffixMatcherCopy);
                }
                if (sslContext != null) {
                    sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                            sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifierCopy);
                } else {
                    if (systemProperties) {
                        sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                                (SSLSocketFactory) SSLSocketFactory.getDefault(),
                                supportedProtocols, supportedCipherSuites, hostnameVerifierCopy);
                    } else {
                        sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                                SSLContexts.createDefault(),
                                hostnameVerifierCopy);
                    }
                }
            }
            @SuppressWarnings("resource")
            final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactoryCopy)
                        .build(),
                    null,
                    null,
                    dnsResolver,
                    connTimeToLive,
                    connTimeToLiveTimeUnit != null ? connTimeToLiveTimeUnit : TimeUnit.MILLISECONDS);
            if (defaultSocketConfig != null) {
                poolingmgr.setDefaultSocketConfig(defaultSocketConfig);
            }
            if (defaultConnectionConfig != null) {
                poolingmgr.setDefaultConnectionConfig(defaultConnectionConfig);
            }
            if (systemProperties) {
                String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    s = System.getProperty("http.maxConnections", "5");
                    final int max = Integer.parseInt(s);
                    poolingmgr.setDefaultMaxPerRoute(max);
                    poolingmgr.setMaxTotal(2 * max);
                }
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
            if (systemProperties) {
                final String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    reuseStrategyCopy = DefaultClientConnectionReuseStrategy.INSTANCE;
                } else {
                    reuseStrategyCopy = NoConnectionReuseStrategy.INSTANCE;
                }
            } else {
                reuseStrategyCopy = DefaultClientConnectionReuseStrategy.INSTANCE;
            }
        }
        ConnectionKeepAliveStrategy keepAliveStrategyCopy = this.keepAliveStrategy;
        if (keepAliveStrategyCopy == null) {
            keepAliveStrategyCopy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        }
        AuthenticationStrategy targetAuthStrategyCopy = this.targetAuthStrategy;
        if (targetAuthStrategyCopy == null) {
            targetAuthStrategyCopy = TargetAuthenticationStrategy.INSTANCE;
        }
        AuthenticationStrategy proxyAuthStrategyCopy = this.proxyAuthStrategy;
        if (proxyAuthStrategyCopy == null) {
            proxyAuthStrategyCopy = ProxyAuthenticationStrategy.INSTANCE;
        }
        UserTokenHandler userTokenHandlerCopy = this.userTokenHandler;
        if (userTokenHandlerCopy == null) {
            if (!connectionStateDisabled) {
                userTokenHandlerCopy = DefaultUserTokenHandler.INSTANCE;
            } else {
                userTokenHandlerCopy = NoopUserTokenHandler.INSTANCE;
            }
        }

        String userAgentCopy = this.userAgent;
        if (userAgentCopy == null) {
            if (systemProperties) {
                userAgentCopy = System.getProperty("http.agent");
            }
            if (userAgentCopy == null && !defaultUserAgentDisabled) {
                userAgentCopy = VersionInfo.getUserAgent("Apache-HttpClient",
                        "top.mrxiaom.pluginbase.resolver.http.client", getClass());
            }
        }

        ClientExecChain execChain = createMainExec(
                requestExecCopy,
                connManagerCopy,
                reuseStrategyCopy,
                keepAliveStrategyCopy,
                new ImmutableHttpProcessor(new RequestTargetHost(), new RequestUserAgent(userAgentCopy)),
                targetAuthStrategyCopy,
                proxyAuthStrategyCopy,
                userTokenHandlerCopy);

        execChain = decorateMainExec(execChain);

        HttpProcessor httpprocessorCopy = this.httpprocessor;
        if (httpprocessorCopy == null) {

            final HttpProcessorBuilder b = HttpProcessorBuilder.create();
            if (requestFirst != null) {
                for (final HttpRequestInterceptor i: requestFirst) {
                    b.addFirst(i);
                }
            }
            if (responseFirst != null) {
                for (final HttpResponseInterceptor i: responseFirst) {
                    b.addFirst(i);
                }
            }
            b.addAll(
                    new RequestDefaultHeaders(defaultHeaders),
                    new RequestContent(),
                    new RequestTargetHost(),
                    new RequestClientConnControl(),
                    new RequestUserAgent(userAgentCopy),
                    new RequestExpectContinue());
            if (!contentCompressionDisabled) {
                if (contentDecoderMap != null) {
                    final List<String> encodings = new ArrayList<>(contentDecoderMap.keySet());
                    Collections.sort(encodings);
                    b.add(new RequestAcceptEncoding(encodings));
                } else {
                    b.add(new RequestAcceptEncoding());
                }
            }
            if (!authCachingDisabled) {
                b.add(new RequestAuthCache());
            }
            if (!contentCompressionDisabled) {
                if (contentDecoderMap != null) {
                    final RegistryBuilder<InputStreamFactory> b2 = RegistryBuilder.create();
                    for (final Map.Entry<String, InputStreamFactory> entry: contentDecoderMap.entrySet()) {
                        b2.register(entry.getKey(), entry.getValue());
                    }
                    b.add(new ResponseContentEncoding(b2.build()));
                } else {
                    b.add(new ResponseContentEncoding());
                }
            }
            if (requestLast != null) {
                for (final HttpRequestInterceptor i: requestLast) {
                    b.addLast(i);
                }
            }
            if (responseLast != null) {
                for (final HttpResponseInterceptor i: responseLast) {
                    b.addLast(i);
                }
            }
            httpprocessorCopy = b.build();
        }
        execChain = new ProtocolExec(execChain, httpprocessorCopy);

        execChain = decorateProtocolExec(execChain);

        // Add request retry executor, if not disabled
        if (!automaticRetriesDisabled) {
            HttpRequestRetryHandler retryHandlerCopy = this.retryHandler;
            if (retryHandlerCopy == null) {
                retryHandlerCopy = DefaultHttpRequestRetryHandler.INSTANCE;
            }
            execChain = new RetryExec(execChain, retryHandlerCopy);
        }

        HttpRoutePlanner routePlannerCopy = this.routePlanner;
        if (routePlannerCopy == null) {
            SchemePortResolver schemePortResolverCopy = this.schemePortResolver;
            if (schemePortResolverCopy == null) {
                schemePortResolverCopy = DefaultSchemePortResolver.INSTANCE;
            }
            if (proxy != null) {
                routePlannerCopy = new DefaultProxyRoutePlanner(proxy, schemePortResolverCopy);
            } else if (systemProperties) {
                routePlannerCopy = new SystemDefaultRoutePlanner(
                        schemePortResolverCopy, ProxySelector.getDefault());
            } else {
                routePlannerCopy = new DefaultRoutePlanner(schemePortResolverCopy);
            }
        }

        // Optionally, add service unavailable retry executor
        final ServiceUnavailableRetryStrategy serviceUnavailStrategyCopy = this.serviceUnavailStrategy;
        if (serviceUnavailStrategyCopy != null) {
            execChain = new ServiceUnavailableRetryExec(execChain, serviceUnavailStrategyCopy);
        }

        // Add redirect executor, if not disabled
        if (!redirectHandlingDisabled) {
            RedirectStrategy redirectStrategyCopy = this.redirectStrategy;
            if (redirectStrategyCopy == null) {
                redirectStrategyCopy = DefaultRedirectStrategy.INSTANCE;
            }
            execChain = new RedirectExec(execChain, routePlannerCopy, redirectStrategyCopy);
        }

        // Optionally, add connection back-off executor
        if (this.backoffManager != null && this.connectionBackoffStrategy != null) {
            execChain = new BackoffStrategyExec(execChain, this.connectionBackoffStrategy, this.backoffManager);
        }

        Lookup<AuthSchemeProvider> authSchemeRegistryCopy = this.authSchemeRegistry;
        if (authSchemeRegistryCopy == null) {
            authSchemeRegistryCopy = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build();
        }

        CredentialsProvider defaultCredentialsProvider = this.credentialsProvider;
        if (defaultCredentialsProvider == null) {
            if (systemProperties) {
                defaultCredentialsProvider = new SystemDefaultCredentialsProvider();
            } else {
                defaultCredentialsProvider = new BasicCredentialsProvider();
            }
        }

        List<Closeable> closeablesCopy = closeables != null ? new ArrayList<>(closeables) : null;
        if (!this.connManagerShared) {
            if (closeablesCopy == null) {
                closeablesCopy = new ArrayList<>(1);
            }
            closeablesCopy.add(connManagerCopy::shutdown);
        }

        return new InternalHttpClient(
                execChain,
                connManagerCopy,
                routePlannerCopy,
                authSchemeRegistryCopy,
                defaultCredentialsProvider,
                defaultRequestConfig != null ? defaultRequestConfig : RequestConfig.DEFAULT,
                closeablesCopy);
    }

}
