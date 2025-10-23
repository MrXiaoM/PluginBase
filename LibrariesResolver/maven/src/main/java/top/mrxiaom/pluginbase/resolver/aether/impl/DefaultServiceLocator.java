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
package top.mrxiaom.pluginbase.resolver.aether.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystem;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultArtifactResolver;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultChecksumPolicyProvider;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultFileProcessor;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultLocalPathComposer;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultLocalRepositoryProvider;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultMetadataResolver;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultOfflineController;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultRemoteRepositoryManager;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultRepositoryConnectorProvider;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultRepositoryEventDispatcher;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultRepositoryLayoutProvider;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultRepositorySystem;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultRepositorySystemLifecycle;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultTrackingFileManager;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultTransporterProvider;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultUpdateCheckManager;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.DefaultUpdatePolicyAnalyzer;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.LocalPathComposer;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.Maven2RepositoryLayoutFactory;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.TrackingFileManager;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.checksum.DefaultChecksumAlgorithmFactorySelector;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.collect.DefaultDependencyCollector;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.filter.DefaultRemoteRepositoryFilterManager;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext.DefaultSyncContextFactory;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactory;
import top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactoryImpl;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.checksum.ChecksumPolicyProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayoutFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayoutProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.TransporterProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.io.FileProcessor;
import top.mrxiaom.pluginbase.resolver.aether.spi.localrepo.LocalRepositoryManagerFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.spi.synccontext.SyncContextFactory;

import static java.util.Objects.requireNonNull;

/**
 * A simple service locator that is already setup with all components from this library. To acquire a complete
 * repository system, clients need to add an artifact descriptor reader, a version resolver, a version range resolver
 * and optionally some repository connector and transporter factories to access remote repositories. Once the locator is
 * fully populated, the repository system can be created like this:
 *
 * <pre>
 * RepositorySystem repoSystem = serviceLocator.getService( RepositorySystem.class );
 * </pre>
 *
 * <em>Note:</em> This class is not thread-safe. Clients are expected to create the service locator and the repository
 * system on a single thread.
 */
public final class DefaultServiceLocator implements ServiceLocator {

    private class Entry<T> {

        private final Class<T> type;

        private final Collection<Object> providers;

        private List<T> instances;

        Entry(Class<T> type) {
            this.type = requireNonNull(type, "service type cannot be null");
            providers = new LinkedHashSet<>(8);
        }

        public synchronized void setService(Class<? extends T> impl) {
            providers.clear();
            addService(impl);
        }

        public synchronized void addService(Class<? extends T> impl) {
            providers.add(requireNonNull(impl, "implementation class cannot be null"));
            instances = null;
        }

        public T getInstance() {
            List<T> instances = getInstances();
            return instances.isEmpty() ? null : instances.get(0);
        }

        public synchronized List<T> getInstances() {
            if (instances == null) {
                instances = new ArrayList<>(providers.size());
                for (Object provider : providers) {
                    T instance;
                    if (provider instanceof Class) {
                        instance = newInstance((Class<?>) provider);
                    } else {
                        instance = type.cast(provider);
                    }
                    if (instance != null) {
                        instances.add(instance);
                    }
                }
                instances = Collections.unmodifiableList(instances);
            }
            return instances;
        }

        private T newInstance(Class<?> impl) {
            try {
                Constructor<?> constr = impl.getDeclaredConstructor();
                if (!Modifier.isPublic(constr.getModifiers())) {
                    constr.setAccessible(true);
                }
                Object obj = constr.newInstance();

                T instance = type.cast(obj);
                if (instance instanceof Service) {
                    ((Service) instance).initService(DefaultServiceLocator.this);
                }
                return instance;
            } catch (Exception | LinkageError ignored) {
            }
            return null;
        }
    }

    private final Map<Class<?>, Entry<?>> entries;

    /**
     * Creates a new service locator that already knows about all service implementations included this library.
     */
    public DefaultServiceLocator() {
        entries = new HashMap<>();

        addService(RepositorySystem.class, DefaultRepositorySystem.class);
        addService(ArtifactResolver.class, DefaultArtifactResolver.class);
        addService(DependencyCollector.class, DefaultDependencyCollector.class);
        addService(MetadataResolver.class, DefaultMetadataResolver.class);
        addService(RepositoryLayoutProvider.class, DefaultRepositoryLayoutProvider.class);
        addService(RepositoryLayoutFactory.class, Maven2RepositoryLayoutFactory.class);
        addService(TransporterProvider.class, DefaultTransporterProvider.class);
        addService(ChecksumPolicyProvider.class, DefaultChecksumPolicyProvider.class);
        addService(RepositoryConnectorProvider.class, DefaultRepositoryConnectorProvider.class);
        addService(RemoteRepositoryManager.class, DefaultRemoteRepositoryManager.class);
        addService(UpdateCheckManager.class, DefaultUpdateCheckManager.class);
        addService(UpdatePolicyAnalyzer.class, DefaultUpdatePolicyAnalyzer.class);
        addService(FileProcessor.class, DefaultFileProcessor.class);
        addService(SyncContextFactory.class, DefaultSyncContextFactory.class);
        addService(RepositoryEventDispatcher.class, DefaultRepositoryEventDispatcher.class);
        addService(OfflineController.class, DefaultOfflineController.class);
        addService(LocalRepositoryProvider.class, DefaultLocalRepositoryProvider.class);
        addService(LocalRepositoryManagerFactory.class, SimpleLocalRepositoryManagerFactory.class);
        addService(LocalRepositoryManagerFactory.class, EnhancedLocalRepositoryManagerFactory.class);
        addService(TrackingFileManager.class, DefaultTrackingFileManager.class);
        addService(ChecksumAlgorithmFactorySelector.class, DefaultChecksumAlgorithmFactorySelector.class);
        addService(LocalPathComposer.class, DefaultLocalPathComposer.class);
        addService(RemoteRepositoryFilterManager.class, DefaultRemoteRepositoryFilterManager.class);
        addService(RepositorySystemLifecycle.class, DefaultRepositorySystemLifecycle.class);
        addService(NamedLockFactoryAdapterFactory.class, NamedLockFactoryAdapterFactoryImpl.class);
    }

    private <T> Entry<T> getEntry(Class<T> type, boolean create) {
        @SuppressWarnings("unchecked")
        Entry<T> entry = (Entry<T>) entries.get(requireNonNull(type, "service type cannot be null"));
        if (entry == null && create) {
            entry = new Entry<>(type);
            entries.put(type, entry);
        }
        return entry;
    }

    /**
     * Adds an implementation class for a service. The specified class must have a no-arg constructor (of any
     * visibility). If the service implementation itself requires other services for its operation, it should implement
     * {@link Service} to gain access to this service locator.
     *
     * @param <T>  The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @param impl The implementation class of the service, must not be {@code null}.
     */
    public <T> void addService(Class<T> type, Class<? extends T> impl) {
        getEntry(type, true).addService(impl);
    }

    public <T> T getService(Class<T> type) {
        Entry<T> entry = getEntry(type, false);
        return (entry != null) ? entry.getInstance() : null;
    }

    public <T> List<T> getServices(Class<T> type) {
        Entry<T> entry = getEntry(type, false);
        return (entry != null) ? entry.getInstances() : null;
    }
}
