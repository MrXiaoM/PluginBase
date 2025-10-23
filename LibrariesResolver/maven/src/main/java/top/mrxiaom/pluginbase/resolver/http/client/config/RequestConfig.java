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

package top.mrxiaom.pluginbase.resolver.http.client.config;

import java.net.InetAddress;

import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;

/**
 *  Immutable class encapsulating request configuration items.
 *  The default setting for stale connection checking changed
 *  to false, and the feature was deprecated starting with version 4.4.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestConfig implements Cloneable {

    public static final RequestConfig DEFAULT = new Builder().build();

    private final boolean expectContinueEnabled;
    private final HttpHost proxy;
    private final InetAddress localAddress;
    private final boolean redirectsEnabled;
    private final boolean relativeRedirectsAllowed;
    private final boolean circularRedirectsAllowed;
    private final int maxRedirects;
    private final int connectionRequestTimeout;
    private final int connectTimeout;
    private final int socketTimeout;
    private final boolean contentCompressionEnabled;
    private final boolean normalizeUri;

    /**
     * Intended for CDI compatibility
    */
    protected RequestConfig() {
        this(false, null, null, false, false, false, 0, 0, 0, 0, true, true);
    }

    RequestConfig(
            final boolean expectContinueEnabled,
            final HttpHost proxy,
            final InetAddress localAddress,
            final boolean redirectsEnabled,
            final boolean relativeRedirectsAllowed,
            final boolean circularRedirectsAllowed,
            final int maxRedirects,
            final int connectionRequestTimeout,
            final int connectTimeout,
            final int socketTimeout,
            final boolean contentCompressionEnabled,
            final boolean normalizeUri) {
        super();
        this.expectContinueEnabled = expectContinueEnabled;
        this.proxy = proxy;
        this.localAddress = localAddress;
        this.redirectsEnabled = redirectsEnabled;
        this.relativeRedirectsAllowed = relativeRedirectsAllowed;
        this.circularRedirectsAllowed = circularRedirectsAllowed;
        this.maxRedirects = maxRedirects;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.contentCompressionEnabled = contentCompressionEnabled;
        this.normalizeUri = normalizeUri;
    }

    /**
     * Determines whether the 'Expect: 100-Continue' handshake is enabled
     * for entity enclosing methods. The purpose of the 'Expect: 100-Continue'
     * handshake is to allow a client that is sending a request message with
     * a request body to determine if the origin server is willing to
     * accept the request (based on the request headers) before the client
     * sends the request body.
     * <p>
     * The use of the 'Expect: 100-continue' handshake can result in
     * a noticeable performance improvement for entity enclosing requests
     * (such as POST and PUT) that require the target server's
     * authentication.
     * </p>
     * <p>
     * 'Expect: 100-continue' handshake should be used with caution, as it
     * may cause problems with HTTP servers and proxies that do not support
     * HTTP/1.1 protocol.
     * </p>
     * <p>
     * Default: {@code false}
     * </p>
     */
    public boolean isExpectContinueEnabled() {
        return expectContinueEnabled;
    }

    /**
     * Returns HTTP proxy to be used for request execution.
     * <p>
     * Default: {@code null}
     * </p>
     */
    public HttpHost getProxy() {
        return proxy;
    }

    /**
     * Returns local address to be used for request execution.
     * <p>
     * On machines with multiple network interfaces, this parameter
     * can be used to select the network interface from which the
     * connection originates.
     * </p>
     * <p>
     * Default: {@code null}
     * </p>
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Determines whether redirects should be handled automatically.
     * <p>
     * Default: {@code true}
     * </p>
     */
    public boolean isRedirectsEnabled() {
        return redirectsEnabled;
    }

    /**
     * Determines whether relative redirects should be rejected. HTTP specification
     * requires the location value be an absolute URI.
     * <p>
     * Default: {@code true}
     * </p>
     */
    public boolean isRelativeRedirectsAllowed() {
        return relativeRedirectsAllowed;
    }

    /**
     * Determines whether circular redirects (redirects to the same location) should
     * be allowed. The HTTP spec is not sufficiently clear whether circular redirects
     * are permitted, therefore optionally they can be enabled
     * <p>
     * Default: {@code false}
     * </p>
     */
    public boolean isCircularRedirectsAllowed() {
        return circularRedirectsAllowed;
    }

    /**
     * Returns the maximum number of redirects to be followed. The limit on number
     * of redirects is intended to prevent infinite loops.
     * <p>
     * Default: {@code 50}
     * </p>
     */
    public int getMaxRedirects() {
        return maxRedirects;
    }

    /**
     * Returns the timeout in milliseconds used when requesting a connection
     * from the connection manager.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default if applicable).
     * </p>
     * <p>
     * Default: {@code -1}
     * </p>
     */
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default if applicable).
     * </p>
     * <p>
     * Default: {@code -1}
     * </p>
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds,
     * which is the timeout for waiting for data or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default if applicable).
     * </p>
     * <p>
     * Default: {@code -1}
     * </p>
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Determines whether the target server is requested to compress content.
     * <p>
     * Default: {@code true}
     * </p>
     *
     * @since 4.5
     */
    public boolean isContentCompressionEnabled() {
        return contentCompressionEnabled;
    }

    /**
     * Determines whether client should normalize URIs in requests or not.
     * <p>
     * Default: {@code true}
     * </p>
     *
     * @since 4.5.8
     */
    public boolean isNormalizeUri() {
        return normalizeUri;
    }

    @Override
    protected RequestConfig clone() throws CloneNotSupportedException {
        return (RequestConfig) super.clone();
    }

    @Override
    public String toString() {
        return "[" +
                "expectContinueEnabled=" + expectContinueEnabled +
                ", proxy=" + proxy +
                ", localAddress=" + localAddress +
                ", redirectsEnabled=" + redirectsEnabled +
                ", relativeRedirectsAllowed=" + relativeRedirectsAllowed +
                ", maxRedirects=" + maxRedirects +
                ", circularRedirectsAllowed=" + circularRedirectsAllowed +
                ", connectionRequestTimeout=" + connectionRequestTimeout +
                ", connectTimeout=" + connectTimeout +
                ", socketTimeout=" + socketTimeout +
                ", contentCompressionEnabled=" + contentCompressionEnabled +
                ", normalizeUri=" + normalizeUri +
                "]";
    }

    public static RequestConfig.Builder custom() {
        return new Builder();
    }

    public static class Builder {

        private boolean expectContinueEnabled;
        private HttpHost proxy;
        private InetAddress localAddress;
        private boolean redirectsEnabled;
        private boolean relativeRedirectsAllowed;
        private int maxRedirects;
        private int connectionRequestTimeout;
        private int connectTimeout;
        private int socketTimeout;
        private boolean contentCompressionEnabled;
        private boolean normalizeUri;

        Builder() {
            super();
            this.redirectsEnabled = true;
            this.maxRedirects = 50;
            this.relativeRedirectsAllowed = true;
            this.connectionRequestTimeout = -1;
            this.connectTimeout = -1;
            this.socketTimeout = -1;
            this.contentCompressionEnabled = true;
            this.normalizeUri = true;
        }

        public Builder setProxy(final HttpHost proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder setLocalAddress(final InetAddress localAddress) {
            this.localAddress = localAddress;
            return this;
        }

        public Builder setConnectionRequestTimeout(final int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder setConnectTimeout(final int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setSocketTimeout(final int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(
                    expectContinueEnabled,
                    proxy,
                    localAddress,
                    redirectsEnabled,
                    relativeRedirectsAllowed,
                    false,
                    maxRedirects,
                    connectionRequestTimeout,
                    connectTimeout,
                    socketTimeout,
                    contentCompressionEnabled,
                    normalizeUri);
        }

    }

}
