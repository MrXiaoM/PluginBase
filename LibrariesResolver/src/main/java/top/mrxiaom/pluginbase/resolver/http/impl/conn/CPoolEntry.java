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
package top.mrxiaom.pluginbase.resolver.http.impl.conn;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import top.mrxiaom.pluginbase.resolver.http.HttpClientConnection;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.conn.ManagedHttpClientConnection;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.HttpRoute;
import top.mrxiaom.pluginbase.resolver.http.pool.PoolEntry;

/**
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.SAFE)
class CPoolEntry extends PoolEntry<HttpRoute, ManagedHttpClientConnection> {

    private volatile boolean routeComplete;

    public CPoolEntry(
            final String id,
            final HttpRoute route,
            final ManagedHttpClientConnection conn,
            final long timeToLive, final TimeUnit timeUnit) {
        super(id, route, conn, timeToLive, timeUnit);
    }

    public void markRouteComplete() {
        this.routeComplete = true;
    }

    public boolean isRouteComplete() {
        return this.routeComplete;
    }

    public void closeConnection() throws IOException {
        final HttpClientConnection conn = getConnection();
        conn.close();
    }

    public void shutdownConnection() throws IOException {
        final HttpClientConnection conn = getConnection();
        conn.shutdown();
    }

    @Override
    public boolean isExpired(final long now) {
        return super.isExpired(now);
    }

    @Override
    public boolean isClosed() {
        final HttpClientConnection conn = getConnection();
        return !conn.isOpen();
    }

    @Override
    public void close() {
        try {
            closeConnection();
        } catch (IOException ignored) {
        }
    }

}
