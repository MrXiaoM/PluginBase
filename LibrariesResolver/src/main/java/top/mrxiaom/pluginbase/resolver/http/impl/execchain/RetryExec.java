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

import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HttpException;
import top.mrxiaom.pluginbase.resolver.http.NoHttpResponseException;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.client.HttpRequestRetryHandler;
import top.mrxiaom.pluginbase.resolver.http.client.NonRepeatableRequestException;
import top.mrxiaom.pluginbase.resolver.http.client.methods.CloseableHttpResponse;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpExecutionAware;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpRequestWrapper;
import top.mrxiaom.pluginbase.resolver.http.client.protocol.HttpClientContext;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.util.Args;

/**
 * Request executor in the request execution chain that is responsible
 * for making a decision whether a request failed due to an I/O error
 * should be re-executed.
 * <p>
 * Further responsibilities such as communication with the opposite
 * endpoint is delegated to the next executor in the request execution
 * chain.
 * </p>
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class RetryExec implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final HttpRequestRetryHandler retryHandler;

    public RetryExec(
            final ClientExecChain requestExecutor,
            final HttpRequestRetryHandler retryHandler) {
        Args.notNull(requestExecutor, "HTTP request executor");
        Args.notNull(retryHandler, "HTTP request retry handler");
        this.requestExecutor = requestExecutor;
        this.retryHandler = retryHandler;
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
        final Header[] origheaders = request.getAllHeaders();
        for (int execCount = 1;; execCount++) {
            try {
                return this.requestExecutor.execute(route, request, context, execAware);
            } catch (final IOException ex) {
                if (execAware != null && execAware.isAborted()) {
                    throw ex;
                }
                if (retryHandler.retryRequest(ex, execCount, context)) {
                    if (!RequestEntityProxy.isRepeatable(request)) {
                        throw new NonRepeatableRequestException("Cannot retry request " +
                                "with a non-repeatable request entity", ex);
                    }
                    request.setHeaders(origheaders);
                } else {
                    if (ex instanceof NoHttpResponseException) {
                        final NoHttpResponseException updatedex = new NoHttpResponseException(
                                route.getTargetHost().toHostString() + " failed to respond");
                        updatedex.setStackTrace(ex.getStackTrace());
                        throw updatedex;
                    }
                    throw ex;
                }
            }
        }
    }

}
