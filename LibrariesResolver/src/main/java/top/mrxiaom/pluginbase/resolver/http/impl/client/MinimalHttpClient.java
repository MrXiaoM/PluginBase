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

import top.mrxiaom.pluginbase.resolver.http.HttpException;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.client.ClientProtocolException;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.client.methods.CloseableHttpResponse;
import top.mrxiaom.pluginbase.resolver.http.client.methods.Configurable;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpExecutionAware;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpRequestWrapper;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.HttpClientContext;
import top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.impl.DefaultConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.impl.execchain.MinimalClientExec;
import top.mrxiaom.pluginbase.resolver.http.protocol.BasicHttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpRequestExecutor;
import top.mrxiaom.pluginbase.resolver.http.util.Args;

/**
 * Internal class.
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
class MinimalHttpClient extends CloseableHttpClient {

    private final HttpClientConnectionManager connManager;
    private final MinimalClientExec requestExecutor;

    public MinimalHttpClient(
            final HttpClientConnectionManager connManager) {
        super();
        this.connManager = Args.notNull(connManager, "HTTP connection manager");
        this.requestExecutor = new MinimalClientExec(
                new HttpRequestExecutor(),
                connManager,
                DefaultConnectionReuseStrategy.INSTANCE,
                DefaultConnectionKeepAliveStrategy.INSTANCE);
    }

    @Override
    protected CloseableHttpResponse doExecute(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context) throws IOException {
        Args.notNull(target, "Target host");
        Args.notNull(request, "HTTP request");
        HttpExecutionAware execAware = null;
        if (request instanceof HttpExecutionAware) {
            execAware = (HttpExecutionAware) request;
        }
        try {
            final HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(request);
            final HttpClientContext localcontext = HttpClientContext.adapt(
                context != null ? context : new BasicHttpContext());
            final HttpRoute route = new HttpRoute(target);
            RequestConfig config = null;
            if (request instanceof Configurable) {
                config = ((Configurable) request).getConfig();
            }
            if (config != null) {
                localcontext.setRequestConfig(config);
            }
            return this.requestExecutor.execute(route, wrapper, localcontext, execAware);
        } catch (final HttpException httpException) {
            throw new ClientProtocolException(httpException);
        }
    }

    @Override
    public void close() {
        this.connManager.shutdown();
    }
}
