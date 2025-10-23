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

import top.mrxiaom.pluginbase.resolver.http.conn.HttpClientConnectionManager;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;

import java.io.Closeable;

/**
 * Container for HTTP-related state that can be shared across invocations of the transporter to optimize the
 * communication with server.
 */
final class LocalState implements Closeable {
    private final GlobalState global;

    private final HttpClientConnectionManager connMgr;

    private final GlobalState.CompoundKey userTokenKey;

    private volatile Object userToken;

    LocalState(RepositorySystemSession session, RemoteRepository repo, ConnMgrConfig connMgrConfig) {
        global = GlobalState.get(session);
        userToken = this;
        if (global == null) {
            connMgr = GlobalState.newConnectionManager(connMgrConfig);
            userTokenKey = null;
        } else {
            connMgr = global.getConnectionManager(connMgrConfig);
            userTokenKey = new GlobalState.CompoundKey(repo.getId(), repo.getUrl(), repo.getProxy());
        }
    }

    public HttpClientConnectionManager getConnectionManager() {
        return connMgr;
    }

    public Object getUserToken() {
        if (userToken == this) {
            userToken = (global != null) ? global.getUserToken(userTokenKey) : null;
        }
        return userToken;
    }

    public void setUserToken(Object userToken) {
        this.userToken = userToken;
        if (global != null) {
            global.setUserToken(userTokenKey, userToken);
        }
    }

    @Override
    public void close() {
        if (global == null) {
            connMgr.shutdown();
        }
    }
}
