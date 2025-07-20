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

import java.util.Collection;

import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.HttpRequestInterceptor;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.client.params.ClientPNames;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.util.Args;

/**
 * Request interceptor that adds default request headers.
 *
 * @since 4.0
 */
@SuppressWarnings("deprecation")
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class RequestDefaultHeaders implements HttpRequestInterceptor {

    private final Collection<? extends Header> defaultHeaders;

    /**
     * @since 4.3
     */
    public RequestDefaultHeaders(final Collection<? extends Header> defaultHeaders) {
        super();
        this.defaultHeaders = defaultHeaders;
    }

    public RequestDefaultHeaders() {
        this(null);
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) {
        Args.notNull(request, "HTTP request");

        final String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase("CONNECT")) {
            return;
        }

        // Add default headers
        @SuppressWarnings("unchecked")
        Collection<? extends Header> defHeaders = (Collection<? extends Header>)
            request.getParams().getParameter(ClientPNames.DEFAULT_HEADERS);
        if (defHeaders == null) {
            defHeaders = this.defaultHeaders;
        }

        if (defHeaders != null) {
            for (final Header defHeader : defHeaders) {
                request.addHeader(defHeader);
            }
        }
    }

}
