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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl.collect;

import java.util.HashMap;
import java.util.Map;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectRequest;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectResult;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyCollectionException;
import top.mrxiaom.pluginbase.resolver.aether.impl.DependencyCollector;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.collect.bf.BfDependencyCollector;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.collect.df.DfDependencyCollector;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.util.ConfigUtils;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation of {@link DependencyCollector} that merely indirect to selected delegate.
 */
public class DefaultDependencyCollector implements DependencyCollector, Service {
    private static final String CONFIG_PROP_COLLECTOR_IMPL = "aether.dependencyCollector.impl";

    private static final String DEFAULT_COLLECTOR_IMPL = DfDependencyCollector.NAME;

    private final Map<String, DependencyCollectorDelegate> delegates;

    /**
     * Default ctor for SL.
     *
     * @deprecated SL is to be removed.
     */
    @Deprecated
    public DefaultDependencyCollector() {
        this.delegates = new HashMap<>();
    }

    public DefaultDependencyCollector(Map<String, DependencyCollectorDelegate> delegates) {
        this.delegates = requireNonNull(delegates);
    }

    @Override
    public void initService(ServiceLocator locator) {
        BfDependencyCollector bf = new BfDependencyCollector();
        bf.initService(locator);
        DfDependencyCollector df = new DfDependencyCollector();
        df.initService(locator);
        this.delegates.put(BfDependencyCollector.NAME, bf);
        this.delegates.put(DfDependencyCollector.NAME, df);
    }

    @Override
    public CollectResult collectDependencies(RepositorySystemSession session, CollectRequest request)
            throws DependencyCollectionException {
        String delegateName = ConfigUtils.getString(session, DEFAULT_COLLECTOR_IMPL, CONFIG_PROP_COLLECTOR_IMPL);
        DependencyCollectorDelegate delegate = delegates.get(delegateName);
        if (delegate == null) {
            throw new IllegalArgumentException(
                    "Unknown collector impl: '" + delegateName + "', known implementations are " + delegates.keySet());
        }
        return delegate.collectDependencies(session, request);
    }
}
