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
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.Transporter;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.TransporterFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.TransporterProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.transfer.NoTransporterException;

import static java.util.Objects.requireNonNull;

public final class DefaultTransporterProvider implements TransporterProvider, Service {
    private Collection<TransporterFactory> factories = new ArrayList<>();

    @Deprecated
    public DefaultTransporterProvider() {
        // enables default constructor
    }

    public DefaultTransporterProvider(Set<TransporterFactory> transporterFactories) {
        setTransporterFactories(transporterFactories);
    }

    public void initService(ServiceLocator locator) {
        setTransporterFactories(locator.getServices(TransporterFactory.class));
    }

    public DefaultTransporterProvider addTransporterFactory(TransporterFactory factory) {
        factories.add(requireNonNull(factory, "transporter factory cannot be null"));
        return this;
    }

    public DefaultTransporterProvider setTransporterFactories(Collection<TransporterFactory> factories) {
        if (factories == null) {
            this.factories = new ArrayList<>();
        } else {
            this.factories = factories;
        }
        return this;
    }

    public Transporter newTransporter(RepositorySystemSession session, RemoteRepository repository)
            throws NoTransporterException {
        requireNonNull(session, "session cannot be null");
        requireNonNull(repository, "repository cannot be null");

        PrioritizedComponents<TransporterFactory> factories = new PrioritizedComponents<>(session);
        for (TransporterFactory factory : this.factories) {
            factories.add(factory, factory.getPriority());
        }

        List<NoTransporterException> errors = new ArrayList<>();
        for (PrioritizedComponent<TransporterFactory> factory : factories.getEnabled()) {
            try {

                return factory.getComponent().newInstance(session, repository);
            } catch (NoTransporterException e) {
                // continue and try next factory
                errors.add(e);
            }
        }

        StringBuilder buffer = new StringBuilder(256);
        if (factories.isEmpty()) {
            buffer.append("No transporter factories registered");
        } else {
            buffer.append("Cannot access ").append(repository.getUrl());
            buffer.append(" using the registered transporter factories: ");
            factories.list(buffer);
        }

        throw new NoTransporterException(repository, buffer.toString(), errors.size() == 1 ? errors.get(0) : null);
    }
}
