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
import top.mrxiaom.pluginbase.resolver.aether.impl.RemoteRepositoryFilterManager;
import top.mrxiaom.pluginbase.resolver.aether.impl.RepositoryConnectorProvider;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.filter.FilteringRepositoryConnector;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.RepositoryConnector;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.RepositoryConnectorFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.filter.RemoteRepositoryFilter;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.transfer.NoRepositoryConnectorException;

import static java.util.Objects.requireNonNull;

public class DefaultRepositoryConnectorProvider implements RepositoryConnectorProvider, Service {

    private Collection<RepositoryConnectorFactory> connectorFactories = new ArrayList<>();

    private RemoteRepositoryFilterManager remoteRepositoryFilterManager;

    @Deprecated
    public DefaultRepositoryConnectorProvider() {
        // enables default constructor
    }

    public DefaultRepositoryConnectorProvider(
            Set<RepositoryConnectorFactory> connectorFactories,
            RemoteRepositoryFilterManager remoteRepositoryFilterManager) {
        setRepositoryConnectorFactories(connectorFactories);
        setRemoteRepositoryFilterManager(remoteRepositoryFilterManager);
    }

    public void initService(ServiceLocator locator) {
        setRepositoryConnectorFactories(locator.getServices(RepositoryConnectorFactory.class));
        setRemoteRepositoryFilterManager(locator.getService(RemoteRepositoryFilterManager.class));
    }

    public DefaultRepositoryConnectorProvider addRepositoryConnectorFactory(RepositoryConnectorFactory factory) {
        connectorFactories.add(requireNonNull(factory, "repository connector factory cannot be null"));
        return this;
    }

    public DefaultRepositoryConnectorProvider setRepositoryConnectorFactories(
            Collection<RepositoryConnectorFactory> factories) {
        if (factories == null) {
            this.connectorFactories = new ArrayList<>();
        } else {
            this.connectorFactories = factories;
        }
        return this;
    }

    public DefaultRepositoryConnectorProvider setRemoteRepositoryFilterManager(
            RemoteRepositoryFilterManager remoteRepositoryFilterManager) {
        this.remoteRepositoryFilterManager = requireNonNull(remoteRepositoryFilterManager);
        return this;
    }

    public RepositoryConnector newRepositoryConnector(RepositorySystemSession session, RemoteRepository repository)
            throws NoRepositoryConnectorException {
        requireNonNull(repository, "remote repository cannot be null");

        if (repository.isBlocked()) {
            if (repository.getMirroredRepositories().isEmpty()) {
                throw new NoRepositoryConnectorException(repository, "Blocked repository: " + repository);
            } else {
                throw new NoRepositoryConnectorException(
                        repository, "Blocked mirror for repositories: " + repository.getMirroredRepositories());
            }
        }

        RemoteRepositoryFilter filter = remoteRepositoryFilterManager.getRemoteRepositoryFilter(session);

        PrioritizedComponents<RepositoryConnectorFactory> factories = new PrioritizedComponents<>(session);
        for (RepositoryConnectorFactory factory : this.connectorFactories) {
            factories.add(factory, factory.getPriority());
        }

        List<NoRepositoryConnectorException> errors = new ArrayList<>();
        for (PrioritizedComponent<RepositoryConnectorFactory> factory : factories.getEnabled()) {
            try {
                RepositoryConnector connector = factory.getComponent().newInstance(session, repository);

                if (filter != null) {
                    return new FilteringRepositoryConnector(repository, connector, filter);
                } else {
                    return connector;
                }
            } catch (NoRepositoryConnectorException e) {
                // continue and try next factory
                errors.add(e);
            }
        }

        StringBuilder buffer = new StringBuilder(256);
        if (factories.isEmpty()) {
            buffer.append("No connector factories available");
        } else {
            buffer.append("Cannot access ").append(repository.getUrl());
            buffer.append(" with type ").append(repository.getContentType());
            buffer.append(" using the available connector factories: ");
            factories.list(buffer);
        }

        throw new NoRepositoryConnectorException(
                repository, buffer.toString(), errors.size() == 1 ? errors.get(0) : null);
    }
}
