/*
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
 */
package top.mrxiaom.pluginbase.resolver.maven.transport;

import top.mrxiaom.pluginbase.resolver.http.config.RegistryBuilder;
import top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager;
import top.mrxiaom.pluginbase.resolver.http.conn.socket.ConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.socket.PlainConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.conn.ssl.NoopHostnameVerifier;
import top.mrxiaom.pluginbase.resolver.http.conn.ssl.SSLConnectionSocketFactory;
import top.mrxiaom.pluginbase.resolver.http.impl.conn.*;
import top.mrxiaom.pluginbase.resolver.http.ssl.SSLContextBuilder;
import top.mrxiaom.pluginbase.resolver.http.ssl.SSLInitializationException;
import top.mrxiaom.pluginbase.resolver.aether.ConfigurationProperties;
import top.mrxiaom.pluginbase.resolver.aether.RepositoryCache;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.util.ConfigUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Container for HTTP-related state that can be shared across incarnations of the transporter to optimize the
 * communication with servers.
 */
final class GlobalState implements Closeable {

    static class CompoundKey {

        private final Object[] keys;

        CompoundKey(Object... keys) {
            this.keys = keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !getClass().equals(obj.getClass())) {
                return false;
            }
            CompoundKey that = (CompoundKey) obj;
            return Arrays.equals(keys, that.keys);
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + Arrays.hashCode(keys);
            return hash;
        }

        @Override
        public String toString() {
            return Arrays.toString(keys);
        }
    }

    private static final String KEY = GlobalState.class.getName();

    private static final String CONFIG_PROP_CACHE_STATE = "aether.connector.http.cacheState";

    private final ConcurrentMap<ConnMgrConfig, HttpClientConnectionManager> connectionManagers;

    private final ConcurrentMap<CompoundKey, Object> userTokens;

    public static GlobalState get(RepositorySystemSession session) {
        GlobalState cache;
        RepositoryCache repoCache = session.getCache();
        if (repoCache == null || !ConfigUtils.getBoolean(session, true, CONFIG_PROP_CACHE_STATE)) {
            cache = null;
        } else {
            Object tmp = repoCache.get(session, KEY);
            if (tmp instanceof GlobalState) {
                cache = (GlobalState) tmp;
            } else {
                synchronized (GlobalState.class) {
                    tmp = repoCache.get(session, KEY);
                    if (tmp instanceof GlobalState) {
                        cache = (GlobalState) tmp;
                    } else {
                        cache = new GlobalState();
                        repoCache.put(session, KEY, cache);
                    }
                }
            }
        }
        return cache;
    }

    private GlobalState() {
        connectionManagers = new ConcurrentHashMap<>();
        userTokens = new ConcurrentHashMap<>();
    }

    @Override
    public void close() {
        for (Iterator<Map.Entry<ConnMgrConfig, HttpClientConnectionManager>> it =
                        connectionManagers.entrySet().iterator();
                it.hasNext(); ) {
            HttpClientConnectionManager connMgr = it.next().getValue();
            it.remove();
            connMgr.shutdown();
        }
    }

    public HttpClientConnectionManager getConnectionManager(ConnMgrConfig config) {
        return connectionManagers.computeIfAbsent(config, GlobalState::newConnectionManager);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public static HttpClientConnectionManager newConnectionManager(ConnMgrConfig connMgrConfig) {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory());
        int connectionMaxTtlSeconds = ConfigurationProperties.DEFAULT_HTTP_CONNECTION_MAX_TTL;
        int maxConnectionsPerRoute = ConfigurationProperties.DEFAULT_HTTP_MAX_CONNECTIONS_PER_ROUTE;

        if (connMgrConfig == null) {
            registryBuilder.register("https", SSLConnectionSocketFactory.getSystemSocketFactory());
        } else {
            // config present: use provided, if any, or create (depending on httpsSecurityMode)
            connectionMaxTtlSeconds = connMgrConfig.connectionMaxTtlSeconds;
            maxConnectionsPerRoute = connMgrConfig.maxConnectionsPerRoute;
            SSLSocketFactory sslSocketFactory;
            HostnameVerifier hostnameVerifier;
            if (ConfigurationProperties.HTTPS_SECURITY_MODE_DEFAULT.equals(connMgrConfig.httpsSecurityMode)) {
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
            } else if (ConfigurationProperties.HTTPS_SECURITY_MODE_INSECURE.equals(connMgrConfig.httpsSecurityMode)) {
                try {
                    sslSocketFactory = new SSLContextBuilder()
                            .loadTrustMaterial(null, (chain, auth) -> true)
                            .build()
                            .getSocketFactory();
                } catch (Exception e) {
                    throw new SSLInitializationException(
                            "Could not configure '" + connMgrConfig.httpsSecurityMode + "' HTTPS security mode", e);
                }
                hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            } else {
                throw new IllegalArgumentException(
                        "Unsupported '" + connMgrConfig.httpsSecurityMode + "' HTTPS security mode.");
            }

            registryBuilder.register(
                    "https",
                    new SSLConnectionSocketFactory(
                            sslSocketFactory, connMgrConfig.protocols, connMgrConfig.cipherSuites, hostnameVerifier));
        }

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(
                new DefaultHttpClientConnectionOperator(
                        registryBuilder.build(), DefaultSchemePortResolver.INSTANCE),
                ManagedHttpClientConnectionFactory.INSTANCE,
                connectionMaxTtlSeconds,
                TimeUnit.SECONDS);
        connMgr.setMaxTotal(maxConnectionsPerRoute * 2);
        connMgr.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        return connMgr;
    }

    public Object getUserToken(CompoundKey key) {
        return userTokens.get(key);
    }

    public void setUserToken(CompoundKey key, Object userToken) {
        if (userToken != null) {
            userTokens.put(key, userToken);
        } else {
            userTokens.remove(key);
        }
    }
}
