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

package top.mrxiaom.pluginbase.resolver.http.protocol;

import java.net.InetAddress;

import top.mrxiaom.pluginbase.resolver.http.HttpConnection;
import top.mrxiaom.pluginbase.resolver.http.HttpException;
import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.HttpInetConnection;
import top.mrxiaom.pluginbase.resolver.http.HttpRequest;
import top.mrxiaom.pluginbase.resolver.http.HttpRequestInterceptor;
import top.mrxiaom.pluginbase.resolver.http.HttpVersion;
import top.mrxiaom.pluginbase.resolver.http.ProtocolException;
import top.mrxiaom.pluginbase.resolver.http.ProtocolVersion;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.util.Args;

/**
 * RequestTargetHost is responsible for adding {@code Host} header. This
 * interceptor is required for client side protocol processors.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestTargetHost implements HttpRequestInterceptor {

    public RequestTargetHost() {
        super();
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException {
        Args.notNull(request, "HTTP request");

        final HttpCoreContext coreContext = HttpCoreContext.adapt(context);

        final ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
        final String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase("CONNECT") && ver.lessEquals(HttpVersion.HTTP_1_0)) {
            return;
        }

        if (!request.containsHeader(HTTP.TARGET_HOST)) {
            HttpHost targetHost = coreContext.getTargetHost();
            if (targetHost == null) {
                final HttpConnection conn = coreContext.getConnection();
                if (conn instanceof HttpInetConnection) {
                    // Populate the context with a default HTTP host based on the
                    // inet address of the target host
                    final InetAddress address = ((HttpInetConnection) conn).getRemoteAddress();
                    final int port = ((HttpInetConnection) conn).getRemotePort();
                    if (address != null) {
                        targetHost = new HttpHost(address.getHostName(), port);
                    }
                }
                if (targetHost == null) {
                    if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                        return;
                    }
                    throw new ProtocolException("Target host missing");
                }
            }
            request.addHeader(HTTP.TARGET_HOST, targetHost.toHostString());
        }
    }

}
