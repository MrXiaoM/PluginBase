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

package top.mrxiaom.pluginbase.resolver.http.client.protocol;

import java.net.URI;
import java.util.List;

import top.mrxiaom.pluginbase.resolver.http.auth.AuthSchemeProvider;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthState;
import top.mrxiaom.pluginbase.resolver.http.client.AuthCache;
import top.mrxiaom.pluginbase.resolver.http.client.CredentialsProvider;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.config.Lookup;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.RouteInfo;
import top.mrxiaom.pluginbase.resolver.http.protocol.BasicHttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpCoreContext;

/**
 * Adaptor class that provides convenience type safe setters and getters
 * for common {@link HttpContext} attributes used in the course
 * of HTTP request execution.
 *
 * @since 4.3
 */
public class HttpClientContext extends HttpCoreContext {

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.conn.routing.RouteInfo}
     * object that represents the actual connection route.
     */
    public static final String HTTP_ROUTE   = "http.route";

    /**
     * Attribute name of a {@link List} object that represents a collection of all
     * redirect locations received in the process of request execution.
     */
    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.client.CredentialsProvider}
     * object that represents the actual credentials provider.
     */
    public static final String CREDS_PROVIDER        = "http.auth.credentials-provider";

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.client.AuthCache} object
     * that represents the auth scheme cache.
     */
    public static final String AUTH_CACHE            = "http.auth.auth-cache";

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.auth.AuthState}
     * object that represents the actual target authentication state.
     */
    public static final String TARGET_AUTH_STATE     = "http.auth.target-scope";

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.auth.AuthState}
     * object that represents the actual proxy authentication state.
     */
    public static final String PROXY_AUTH_STATE      = "http.auth.proxy-scope";

    /**
     * Attribute name of a {@link java.lang.Object} object that represents
     * the actual user identity such as user {@link java.security.Principal}.
     */
    public static final String USER_TOKEN            = "http.user-token";

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.config.Lookup} object that represents
     * the actual {@link AuthSchemeProvider} registry.
     */
    public static final String AUTHSCHEME_REGISTRY   = "http.authscheme-registry";

    /**
     * Attribute name of a {@link top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig} object that
     * represents the actual request configuration.
     */
    public static final String REQUEST_CONFIG = "http.request-config";

    public static HttpClientContext adapt(final HttpContext context) {
        return context instanceof HttpClientContext
                        ? (HttpClientContext) context
                        : new HttpClientContext(context);
    }

    public static HttpClientContext create() {
        return new HttpClientContext(new BasicHttpContext());
    }

    public HttpClientContext(final HttpContext context) {
        super(context);
    }

    public HttpClientContext() {
        super();
    }

    public RouteInfo getHttpRoute() {
        return getAttribute(HTTP_ROUTE, HttpRoute.class);
    }

    @SuppressWarnings("unchecked")
    public List<URI> getRedirectLocations() {
        return getAttribute(REDIRECT_LOCATIONS, List.class);
    }

    @SuppressWarnings("unchecked")
    private <T> Lookup<T> getLookup(final String name, final Class<T> clazz) {
        return getAttribute(name, Lookup.class);
    }

    public Lookup<AuthSchemeProvider> getAuthSchemeRegistry() {
        return getLookup(AUTHSCHEME_REGISTRY, AuthSchemeProvider.class);
    }

    public void setAuthSchemeRegistry(final Lookup<AuthSchemeProvider> lookup) {
        setAttribute(AUTHSCHEME_REGISTRY, lookup);
    }

    public CredentialsProvider getCredentialsProvider() {
        return getAttribute(CREDS_PROVIDER, CredentialsProvider.class);
    }

    public void setCredentialsProvider(final CredentialsProvider credentialsProvider) {
        setAttribute(CREDS_PROVIDER, credentialsProvider);
    }

    public AuthCache getAuthCache() {
        return getAttribute(AUTH_CACHE, AuthCache.class);
    }

    public void setAuthCache(final AuthCache authCache) {
        setAttribute(AUTH_CACHE, authCache);
    }

    public AuthState getTargetAuthState() {
        return getAttribute(TARGET_AUTH_STATE, AuthState.class);
    }

    public AuthState getProxyAuthState() {
        return getAttribute(PROXY_AUTH_STATE, AuthState.class);
    }

    public <T> T getUserToken(final Class<T> clazz) {
        return getAttribute(USER_TOKEN, clazz);
    }

    public Object getUserToken() {
        return getAttribute(USER_TOKEN);
    }

    public void setUserToken(final Object obj) {
        setAttribute(USER_TOKEN, obj);
    }

    public RequestConfig getRequestConfig() {
        final RequestConfig config = getAttribute(REQUEST_CONFIG, RequestConfig.class);
        return config != null ? config : RequestConfig.DEFAULT;
    }

    public void setRequestConfig(final RequestConfig config) {
        setAttribute(REQUEST_CONFIG, config);
    }

}
