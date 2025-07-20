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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.SyncContext;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext.named.NamedLockFactoryAdapter;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.spi.synccontext.SyncContextFactory;

import static java.util.Objects.requireNonNull;

/**
 * Default {@link SyncContextFactory} implementation that uses named locks.
 * <p>
 * The implementation relies fully on {@link NamedLockFactoryAdapterFactory} and all it does is just "stuff" the
 * adapter instance into session, hence factory is called only when given session has no instance created.
 */
public final class DefaultSyncContextFactory implements SyncContextFactory, Service {
    private static final String ADAPTER_KEY = DefaultSyncContextFactory.class.getName() + ".adapter";

    private NamedLockFactoryAdapterFactory namedLockFactoryAdapterFactory;

    /**
     * Constructor used with DI, where factories are injected and selected based on key.
     */
    public DefaultSyncContextFactory(final NamedLockFactoryAdapterFactory namedLockFactoryAdapterFactory) {
        this.namedLockFactoryAdapterFactory = requireNonNull(namedLockFactoryAdapterFactory);
    }

    /**
     * ServiceLocator default ctor.
     *
     * @deprecated Will be removed once ServiceLocator removed.
     */
    @Deprecated
    public DefaultSyncContextFactory() {
        // ctor for ServiceLoader
    }

    @Override
    public void initService(final ServiceLocator locator) {
        this.namedLockFactoryAdapterFactory = requireNonNull(locator.getService(NamedLockFactoryAdapterFactory.class));
    }

    @Override
    public SyncContext newInstance(final RepositorySystemSession session, final boolean shared) {
        requireNonNull(session, "session cannot be null");
        NamedLockFactoryAdapter adapter = (NamedLockFactoryAdapter) session.getData()
                .computeIfAbsent(ADAPTER_KEY, () -> namedLockFactoryAdapterFactory.getAdapter(session));
        return adapter.newInstance(session, shared);
    }
}
