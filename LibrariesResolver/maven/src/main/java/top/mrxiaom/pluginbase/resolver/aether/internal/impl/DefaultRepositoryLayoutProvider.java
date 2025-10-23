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
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayout;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayoutFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayoutProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.transfer.NoRepositoryLayoutException;

import static java.util.Objects.requireNonNull;

public final class DefaultRepositoryLayoutProvider implements RepositoryLayoutProvider, Service {

    private Collection<RepositoryLayoutFactory> factories = new ArrayList<>();

    @Deprecated
    public DefaultRepositoryLayoutProvider() {
        // enables default constructor
    }

    public DefaultRepositoryLayoutProvider(Set<RepositoryLayoutFactory> layoutFactories) {
        setRepositoryLayoutFactories(layoutFactories);
    }

    public void initService(ServiceLocator locator) {
        setRepositoryLayoutFactories(locator.getServices(RepositoryLayoutFactory.class));
    }

    public DefaultRepositoryLayoutProvider addRepositoryLayoutFactory(RepositoryLayoutFactory factory) {
        factories.add(requireNonNull(factory, "layout factory cannot be null"));
        return this;
    }

    public DefaultRepositoryLayoutProvider setRepositoryLayoutFactories(Collection<RepositoryLayoutFactory> factories) {
        if (factories == null) {
            this.factories = new ArrayList<>();
        } else {
            this.factories = factories;
        }
        return this;
    }

    public RepositoryLayout newRepositoryLayout(RepositorySystemSession session, RemoteRepository repository)
            throws NoRepositoryLayoutException {
        requireNonNull(session, "session cannot be null");
        requireNonNull(repository, "remote repository cannot be null");

        PrioritizedComponents<RepositoryLayoutFactory> factories = new PrioritizedComponents<>(session);
        for (RepositoryLayoutFactory factory : this.factories) {
            factories.add(factory, factory.getPriority());
        }

        List<NoRepositoryLayoutException> errors = new ArrayList<>();
        for (PrioritizedComponent<RepositoryLayoutFactory> factory : factories.getEnabled()) {
            try {
                return factory.getComponent().newInstance(session, repository);
            } catch (NoRepositoryLayoutException e) {
                // continue and try next factory
                errors.add(e);
            }
        }

        StringBuilder buffer = new StringBuilder(256);
        if (factories.isEmpty()) {
            buffer.append("No layout factories registered");
        } else {
            buffer.append("Cannot access ").append(repository.getUrl());
            buffer.append(" with type ").append(repository.getContentType());
            buffer.append(" using the available layout factories: ");
            factories.list(buffer);
        }

        throw new NoRepositoryLayoutException(repository, buffer.toString(), errors.size() == 1 ? errors.get(0) : null);
    }
}
