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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.impl.LocalRepositoryProvider;
import top.mrxiaom.pluginbase.resolver.aether.repository.LocalRepository;
import top.mrxiaom.pluginbase.resolver.aether.repository.LocalRepositoryManager;
import top.mrxiaom.pluginbase.resolver.aether.repository.NoLocalRepositoryManagerException;
import top.mrxiaom.pluginbase.resolver.aether.spi.localrepo.LocalRepositoryManagerFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;

import static java.util.Objects.requireNonNull;

public class DefaultLocalRepositoryProvider implements LocalRepositoryProvider, Service {

    private Collection<LocalRepositoryManagerFactory> managerFactories = new ArrayList<>();

    @Deprecated
    public DefaultLocalRepositoryProvider() {
        // enables default constructor
    }

    public DefaultLocalRepositoryProvider(Set<LocalRepositoryManagerFactory> factories) {
        setLocalRepositoryManagerFactories(factories);
    }

    public void initService(ServiceLocator locator) {
        setLocalRepositoryManagerFactories(locator.getServices(LocalRepositoryManagerFactory.class));
    }

    public DefaultLocalRepositoryProvider addLocalRepositoryManagerFactory(LocalRepositoryManagerFactory factory) {
        managerFactories.add(requireNonNull(factory, "local repository manager factory cannot be null"));
        return this;
    }

    public DefaultLocalRepositoryProvider setLocalRepositoryManagerFactories(
            Collection<LocalRepositoryManagerFactory> factories) {
        if (factories == null) {
            managerFactories = new ArrayList<>(2);
        } else {
            managerFactories = factories;
        }
        return this;
    }

    public LocalRepositoryManager newLocalRepositoryManager(RepositorySystemSession session, LocalRepository repository)
            throws NoLocalRepositoryManagerException {
        requireNonNull(session, "session cannot be null");
        requireNonNull(repository, "repository cannot be null");
        PrioritizedComponents<LocalRepositoryManagerFactory> factories = new PrioritizedComponents<>(session);
        for (LocalRepositoryManagerFactory factory : this.managerFactories) {
            factories.add(factory, factory.getPriority());
        }

        List<NoLocalRepositoryManagerException> errors = new ArrayList<>();
        for (PrioritizedComponent<LocalRepositoryManagerFactory> factory : factories.getEnabled()) {
            try {
                return factory.getComponent().newInstance(session, repository);
            } catch (NoLocalRepositoryManagerException e) {
                // continue and try next factory
                errors.add(e);
            }
        }

        StringBuilder buffer = new StringBuilder(256);
        if (factories.isEmpty()) {
            buffer.append("No local repository managers registered");
        } else {
            buffer.append("Cannot access ").append(repository.getBasedir());
            buffer.append(" with type ").append(repository.getContentType());
            buffer.append(" using the available factories ");
            factories.list(buffer);
        }

        throw new NoLocalRepositoryManagerException(
                repository, buffer.toString(), errors.size() == 1 ? errors.get(0) : null);
    }
}
