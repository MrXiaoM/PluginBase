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
import java.util.concurrent.atomic.AtomicBoolean;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystem;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.RequestTrace;
import top.mrxiaom.pluginbase.resolver.aether.SyncContext;
import top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectRequest;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectResult;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyCollectionException;
import top.mrxiaom.pluginbase.resolver.aether.graph.DependencyFilter;
import top.mrxiaom.pluginbase.resolver.aether.graph.DependencyVisitor;
import top.mrxiaom.pluginbase.resolver.aether.impl.ArtifactDescriptorReader;
import top.mrxiaom.pluginbase.resolver.aether.impl.ArtifactResolver;
import top.mrxiaom.pluginbase.resolver.aether.impl.DependencyCollector;
import top.mrxiaom.pluginbase.resolver.aether.impl.LocalRepositoryProvider;
import top.mrxiaom.pluginbase.resolver.aether.impl.MetadataResolver;
import top.mrxiaom.pluginbase.resolver.aether.impl.RemoteRepositoryManager;
import top.mrxiaom.pluginbase.resolver.aether.impl.RepositorySystemLifecycle;
import top.mrxiaom.pluginbase.resolver.aether.impl.VersionRangeResolver;
import top.mrxiaom.pluginbase.resolver.aether.impl.VersionResolver;
import top.mrxiaom.pluginbase.resolver.aether.repository.LocalRepository;
import top.mrxiaom.pluginbase.resolver.aether.repository.LocalRepositoryManager;
import top.mrxiaom.pluginbase.resolver.aether.repository.NoLocalRepositoryManagerException;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactDescriptorException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactDescriptorRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactDescriptorResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactResolutionException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.DependencyRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.DependencyResolutionException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.DependencyResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.MetadataRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.MetadataResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeResolutionException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionResolutionException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionResult;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.Service;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.spi.synccontext.SyncContextFactory;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.visitor.FilteringDependencyVisitor;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.visitor.TreeDependencyVisitor;

import static java.util.Objects.requireNonNull;

public class DefaultRepositorySystem implements RepositorySystem, Service {
    private final AtomicBoolean shutdown;

    private VersionResolver versionResolver;

    private VersionRangeResolver versionRangeResolver;

    private ArtifactResolver artifactResolver;

    private MetadataResolver metadataResolver;

    private ArtifactDescriptorReader artifactDescriptorReader;

    private DependencyCollector dependencyCollector;

    private LocalRepositoryProvider localRepositoryProvider;

    private SyncContextFactory syncContextFactory;

    private RemoteRepositoryManager remoteRepositoryManager;

    private RepositorySystemLifecycle repositorySystemLifecycle;

    @Deprecated
    public DefaultRepositorySystem() {
        // enables default constructor
        this.shutdown = new AtomicBoolean(false);
    }

    @Override
    public void initService(ServiceLocator locator) {
        setVersionResolver(locator.getService(VersionResolver.class));
        setVersionRangeResolver(locator.getService(VersionRangeResolver.class));
        setArtifactResolver(locator.getService(ArtifactResolver.class));
        setMetadataResolver(locator.getService(MetadataResolver.class));
        setArtifactDescriptorReader(locator.getService(ArtifactDescriptorReader.class));
        setDependencyCollector(locator.getService(DependencyCollector.class));
        setLocalRepositoryProvider(locator.getService(LocalRepositoryProvider.class));
        setRemoteRepositoryManager(locator.getService(RemoteRepositoryManager.class));
        setSyncContextFactory(locator.getService(SyncContextFactory.class));
        setRepositorySystemLifecycle(locator.getService(RepositorySystemLifecycle.class));
    }

    public DefaultRepositorySystem setVersionResolver(VersionResolver versionResolver) {
        this.versionResolver = requireNonNull(versionResolver, "version resolver cannot be null");
        return this;
    }

    public DefaultRepositorySystem setVersionRangeResolver(VersionRangeResolver versionRangeResolver) {
        this.versionRangeResolver = requireNonNull(versionRangeResolver, "version range resolver cannot be null");
        return this;
    }

    public DefaultRepositorySystem setArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = requireNonNull(artifactResolver, "artifact resolver cannot be null");
        return this;
    }

    public DefaultRepositorySystem setMetadataResolver(MetadataResolver metadataResolver) {
        this.metadataResolver = requireNonNull(metadataResolver, "metadata resolver cannot be null");
        return this;
    }

    public DefaultRepositorySystem setArtifactDescriptorReader(ArtifactDescriptorReader artifactDescriptorReader) {
        this.artifactDescriptorReader =
                requireNonNull(artifactDescriptorReader, "artifact descriptor reader cannot be null");
        return this;
    }

    public DefaultRepositorySystem setDependencyCollector(DependencyCollector dependencyCollector) {
        this.dependencyCollector = requireNonNull(dependencyCollector, "dependency collector cannot be null");
        return this;
    }

    public DefaultRepositorySystem setLocalRepositoryProvider(LocalRepositoryProvider localRepositoryProvider) {
        this.localRepositoryProvider =
                requireNonNull(localRepositoryProvider, "local repository provider cannot be null");
        return this;
    }

    public DefaultRepositorySystem setSyncContextFactory(SyncContextFactory syncContextFactory) {
        this.syncContextFactory = requireNonNull(syncContextFactory, "sync context factory cannot be null");
        return this;
    }

    public DefaultRepositorySystem setRemoteRepositoryManager(RemoteRepositoryManager remoteRepositoryManager) {
        this.remoteRepositoryManager =
                requireNonNull(remoteRepositoryManager, "remote repository provider cannot be null");
        return this;
    }

    public DefaultRepositorySystem setRepositorySystemLifecycle(RepositorySystemLifecycle repositorySystemLifecycle) {
        this.repositorySystemLifecycle =
                requireNonNull(repositorySystemLifecycle, "repository system lifecycle cannot be null");
        return this;
    }

    @Override
    public VersionResult resolveVersion(RepositorySystemSession session, VersionRequest request)
            throws VersionResolutionException {
        validateSession(session);
        requireNonNull(request, "request cannot be null");

        return versionResolver.resolveVersion(session, request);
    }

    @Override
    public VersionRangeResult resolveVersionRange(RepositorySystemSession session, VersionRangeRequest request)
            throws VersionRangeResolutionException {
        validateSession(session);
        requireNonNull(request, "request cannot be null");

        return versionRangeResolver.resolveVersionRange(session, request);
    }

    @Override
    public ArtifactDescriptorResult readArtifactDescriptor(
            RepositorySystemSession session, ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
        validateSession(session);
        requireNonNull(request, "request cannot be null");

        return artifactDescriptorReader.readArtifactDescriptor(session, request);
    }

    @Override
    public ArtifactResult resolveArtifact(RepositorySystemSession session, ArtifactRequest request)
            throws ArtifactResolutionException {
        validateSession(session);
        requireNonNull(request, "request cannot be null");

        return artifactResolver.resolveArtifact(session, request);
    }

    @Override
    public List<ArtifactResult> resolveArtifacts(
            RepositorySystemSession session, Collection<? extends ArtifactRequest> requests)
            throws ArtifactResolutionException {
        validateSession(session);
        requireNonNull(requests, "requests cannot be null");

        return artifactResolver.resolveArtifacts(session, requests);
    }

    @Override
    public List<MetadataResult> resolveMetadata(
            RepositorySystemSession session, Collection<? extends MetadataRequest> requests) {
        validateSession(session);
        requireNonNull(requests, "requests cannot be null");

        return metadataResolver.resolveMetadata(session, requests);
    }

    @Override
    public CollectResult collectDependencies(RepositorySystemSession session, CollectRequest request)
            throws DependencyCollectionException {
        validateSession(session);
        requireNonNull(request, "request cannot be null");

        return dependencyCollector.collectDependencies(session, request);
    }

    @Override
    public DependencyResult resolveDependencies(RepositorySystemSession session, DependencyRequest request)
            throws DependencyResolutionException {
        validateSession(session);
        requireNonNull(request, "request cannot be null");

        RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);

        DependencyResult result = new DependencyResult(request);

        DependencyCollectionException dce = null;
        ArtifactResolutionException are = null;

        if (request.getRoot() != null) {
            result.setRoot(request.getRoot());
        } else if (request.getCollectRequest() != null) {
            CollectResult collectResult;
            try {
                request.getCollectRequest().setTrace(trace);
                collectResult = dependencyCollector.collectDependencies(session, request.getCollectRequest());
            } catch (DependencyCollectionException e) {
                dce = e;
                collectResult = e.getResult();
            }
            result.setRoot(collectResult.getRoot());
            result.setCollectExceptions(collectResult.getExceptions());
        } else {
            throw new NullPointerException("dependency node and collect request cannot be null");
        }

        ArtifactRequestBuilder builder = new ArtifactRequestBuilder(trace);
        DependencyFilter filter = request.getFilter();
        DependencyVisitor visitor = (filter != null) ? new FilteringDependencyVisitor(builder, filter) : builder;
        visitor = new TreeDependencyVisitor(visitor);

        if (result.getRoot() != null) {
            result.getRoot().accept(visitor);
        }

        List<ArtifactRequest> requests = builder.getRequests();

        List<ArtifactResult> results;
        try {
            results = artifactResolver.resolveArtifacts(session, requests);
        } catch (ArtifactResolutionException e) {
            are = e;
            results = e.getResults();
        }
        result.setArtifactResults(results);

        updateNodesWithResolvedArtifacts(results);

        if (dce != null) {
            throw new DependencyResolutionException(result, dce);
        } else if (are != null) {
            throw new DependencyResolutionException(result, are);
        }

        return result;
    }

    private void updateNodesWithResolvedArtifacts(List<ArtifactResult> results) {
        for (ArtifactResult result : results) {
            Artifact artifact = result.getArtifact();
            if (artifact != null) {
                result.getRequest().getDependencyNode().setArtifact(artifact);
            }
        }
    }

    @Override
    public LocalRepositoryManager newLocalRepositoryManager(
            RepositorySystemSession session, LocalRepository localRepository) {
        requireNonNull(session, "session cannot be null");
        requireNonNull(localRepository, "localRepository cannot be null");

        try {
            return localRepositoryProvider.newLocalRepositoryManager(session, localRepository);
        } catch (NoLocalRepositoryManagerException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public SyncContext newSyncContext(RepositorySystemSession session, boolean shared) {
        validateSession(session);
        return syncContextFactory.newInstance(session, shared);
    }

    @Override
    public List<RemoteRepository> newResolutionRepositories(
            RepositorySystemSession session, List<RemoteRepository> repositories) {
        validateSession(session);
        validateRepositories(repositories);

        repositories = remoteRepositoryManager.aggregateRepositories(session, new ArrayList<>(), repositories, true);
        return repositories;
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            repositorySystemLifecycle.systemEnded();
        }
    }

    private void validateSession(RepositorySystemSession session) {
        requireNonNull(session, "repository system session cannot be null");
        invalidSession(session.getLocalRepositoryManager(), "local repository manager");
        invalidSession(session.getSystemProperties(), "system properties");
        invalidSession(session.getUserProperties(), "user properties");
        invalidSession(session.getConfigProperties(), "config properties");
        invalidSession(session.getMirrorSelector(), "mirror selector");
        invalidSession(session.getProxySelector(), "proxy selector");
        invalidSession(session.getArtifactTypeRegistry(), "artifact type registry");
        invalidSession(session.getData(), "data");
        if (shutdown.get()) {
            throw new IllegalStateException("repository system is already shut down");
        }
    }

    private void validateRepositories(List<RemoteRepository> repositories) {
        requireNonNull(repositories, "repositories cannot be null");
        for (RemoteRepository repository : repositories) {
            requireNonNull(repository, "repository cannot be null");
        }
    }

    private void invalidSession(Object obj, String name) {
        requireNonNull(obj, "repository system session's " + name + " cannot be null");
    }
}
