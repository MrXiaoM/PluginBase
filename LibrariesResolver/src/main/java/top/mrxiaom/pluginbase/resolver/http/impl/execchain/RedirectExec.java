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

package top.mrxiaom.pluginbase.resolver.http.impl.execchain;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import top.mrxiaom.pluginbase.resolver.http.HttpEntityEnclosingRequest;
import top.mrxiaom.pluginbase.resolver.http.HttpException;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.ProtocolException;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.auth.AuthState;
import top.mrxiaom.pluginbase.resolver.http.client.RedirectException;
import top.mrxiaom.pluginbase.resolver.http.client.RedirectStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.client.methods.CloseableHttpResponse;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpExecutionAware;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpRequestWrapper;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.HttpClientContext;
import top.mrxiaom.pluginbase.resolver.http.client.utils.URIUtils;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoutePlanner;
import top.mrxiaom.pluginbase.resolver.http.util.Args;
import top.mrxiaom.pluginbase.resolver.http.util.EntityUtils;

/**
 * Request executor in the request execution chain that is responsible
 * for handling of request redirects.
 * <p>
 * Further responsibilities such as communication with the opposite
 * endpoint is delegated to the next executor in the request execution
 * chain.
 * </p>
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class RedirectExec implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final RedirectStrategy redirectStrategy;
    private final HttpRoutePlanner routePlanner;

    public RedirectExec(
            final ClientExecChain requestExecutor,
            final HttpRoutePlanner routePlanner,
            final RedirectStrategy redirectStrategy) {
        super();
        Args.notNull(requestExecutor, "HTTP client request executor");
        Args.notNull(routePlanner, "HTTP route planner");
        Args.notNull(redirectStrategy, "HTTP redirect strategy");
        this.requestExecutor = requestExecutor;
        this.routePlanner = routePlanner;
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    public CloseableHttpResponse execute(
            final HttpRoute route,
            final HttpRequestWrapper request,
            final HttpClientContext context,
            final HttpExecutionAware execAware) throws IOException, HttpException {
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");

        final List<URI> redirectLocations = context.getRedirectLocations();
        if (redirectLocations != null) {
            redirectLocations.clear();
        }

        final RequestConfig config = context.getRequestConfig();
        final int maxRedirects = config.getMaxRedirects() > 0 ? config.getMaxRedirects() : 50;
        HttpRoute currentRoute = route;
        HttpRequestWrapper currentRequest = request;
        for (int redirectCount = 0;;) {
            final CloseableHttpResponse response = requestExecutor.execute(
                    currentRoute, currentRequest, context, execAware);
            try {
                if (config.isRedirectsEnabled() &&
                        this.redirectStrategy.isRedirected(currentRequest.getOriginal(), response, context)) {
                    if (!RequestEntityProxy.isRepeatable(currentRequest)) {
                        return response;
                    }
                    if (redirectCount >= maxRedirects) {
                        throw new RedirectException("Maximum redirects ("+ maxRedirects + ") exceeded");
                    }
                    redirectCount++;

                    final HttpRequest redirect = this.redirectStrategy.getRedirect(
                            currentRequest.getOriginal(), response, context);
                    if (!redirect.headerIterator().hasNext()) {
                        final HttpRequest original = request.getOriginal();
                        redirect.setHeaders(original.getAllHeaders());
                    }
                    currentRequest = HttpRequestWrapper.wrap(redirect);

                    if (currentRequest instanceof HttpEntityEnclosingRequest) {
                        RequestEntityProxy.enhance((HttpEntityEnclosingRequest) currentRequest);
                    }

                    final URI uri = currentRequest.getURI();
                    final HttpHost newTarget = URIUtils.extractHost(uri);
                    if (newTarget == null) {
                        throw new ProtocolException("Redirect URI does not specify a valid host name: " +
                                uri);
                    }

                    // Reset virtual host and auth states if redirecting to another host
                    if (!currentRoute.getTargetHost().equals(newTarget)) {
                        final AuthState targetAuthState = context.getTargetAuthState();
                        if (targetAuthState != null) {
                            targetAuthState.reset();
                        }
                        final AuthState proxyAuthState = context.getProxyAuthState();
                        if (proxyAuthState != null && proxyAuthState.isConnectionBased()) {
                            proxyAuthState.reset();
                        }
                    }

                    currentRoute = this.routePlanner.determineRoute(newTarget, currentRequest, context);
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } else {
                    return response;
                }
            } catch (final RuntimeException | IOException ex) {
                response.close();
                throw ex;
            } catch (final HttpException ex) {
                // Protocol exception related to a direct.
                // The underlying connection may still be salvaged.
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException ignored) {
                } finally {
                    response.close();
                }
                throw ex;
            }
        }
    }

}
