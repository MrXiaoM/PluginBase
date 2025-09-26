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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.mrxiaom.pluginbase.resolver.aether.DefaultRepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.RepositoryException;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.RequestTrace;
import top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact;
import top.mrxiaom.pluginbase.resolver.aether.artifact.ArtifactProperties;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectRequest;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectResult;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyCollectionException;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyGraphTransformer;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyTraverser;
import top.mrxiaom.pluginbase.resolver.aether.collection.VersionFilter;
import top.mrxiaom.pluginbase.resolver.aether.graph.DefaultDependencyNode;
import top.mrxiaom.pluginbase.resolver.aether.graph.Dependency;
import top.mrxiaom.pluginbase.resolver.aether.graph.DependencyNode;
import top.mrxiaom.pluginbase.resolver.aether.impl.ArtifactDescriptorReader;
import top.mrxiaom.pluginbase.resolver.aether.impl.DependencyCollector;
import top.mrxiaom.pluginbase.resolver.aether.impl.RemoteRepositoryManager;
import top.mrxiaom.pluginbase.resolver.aether.impl.VersionRangeResolver;
import top.mrxiaom.pluginbase.resolver.aether.repository.ArtifactRepository;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactDescriptorException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactDescriptorRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactDescriptorResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeResolutionException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeResult;
import top.mrxiaom.pluginbase.resolver.aether.spi.locator.ServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.util.ConfigUtils;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.transformer.TransformationContextKeys;
import top.mrxiaom.pluginbase.resolver.aether.version.Version;

import static java.util.Objects.requireNonNull;

/**
 * Helper class for delegate implementations, they MUST subclass this class.
 *
 * @since 1.8.0
 */
public abstract class DependencyCollectorDelegate implements DependencyCollector {
    protected static final String CONFIG_PROP_MAX_EXCEPTIONS = "aether.dependencyCollector.maxExceptions";

    protected static final int CONFIG_PROP_MAX_EXCEPTIONS_DEFAULT = 50;

    protected static final String CONFIG_PROP_MAX_CYCLES = "aether.dependencyCollector.maxCycles";

    protected static final int CONFIG_PROP_MAX_CYCLES_DEFAULT = 10;

    protected RemoteRepositoryManager remoteRepositoryManager;

    protected ArtifactDescriptorReader descriptorReader;

    protected VersionRangeResolver versionRangeResolver;

    /**
     * Default ctor for SL.
     */
    protected DependencyCollectorDelegate() {
        // enables default constructor
    }

    protected DependencyCollectorDelegate(
            RemoteRepositoryManager remoteRepositoryManager,
            ArtifactDescriptorReader artifactDescriptorReader,
            VersionRangeResolver versionRangeResolver) {
        setRemoteRepositoryManager(remoteRepositoryManager);
        setArtifactDescriptorReader(artifactDescriptorReader);
        setVersionRangeResolver(versionRangeResolver);
    }

    public void initService(ServiceLocator locator) {
        setRemoteRepositoryManager(locator.getService(RemoteRepositoryManager.class));
        setArtifactDescriptorReader(locator.getService(ArtifactDescriptorReader.class));
        setVersionRangeResolver(locator.getService(VersionRangeResolver.class));
    }

    public DependencyCollector setRemoteRepositoryManager(RemoteRepositoryManager remoteRepositoryManager) {
        this.remoteRepositoryManager =
                requireNonNull(remoteRepositoryManager, "remote repository manager cannot be null");
        return this;
    }

    public DependencyCollector setArtifactDescriptorReader(ArtifactDescriptorReader artifactDescriptorReader) {
        descriptorReader = requireNonNull(artifactDescriptorReader, "artifact descriptor reader cannot be null");
        return this;
    }

    public DependencyCollector setVersionRangeResolver(VersionRangeResolver versionRangeResolver) {
        this.versionRangeResolver = requireNonNull(versionRangeResolver, "version range resolver cannot be null");
        return this;
    }

    @SuppressWarnings("checkstyle:methodlength")
    @Override
    public final CollectResult collectDependencies(RepositorySystemSession session, CollectRequest request)
            throws DependencyCollectionException {
        requireNonNull(session, "session cannot be null");
        requireNonNull(request, "request cannot be null");
        session = optimizeSession(session);

        RequestTrace trace = RequestTrace.newChild(request.getTrace(), request);

        CollectResult result = new CollectResult(request);

        DependencyTraverser depTraverser = session.getDependencyTraverser();
        VersionFilter verFilter = session.getVersionFilter();

        Dependency root = request.getRoot();
        List<RemoteRepository> repositories = request.getRepositories();
        List<Dependency> dependencies = request.getDependencies();
        List<Dependency> managedDependencies = request.getManagedDependencies();

        Map<String, Object> stats = new LinkedHashMap<>();
        long time1 = System.nanoTime();

        DefaultDependencyNode node;
        if (root != null) {
            List<? extends Version> versions;
            VersionRangeResult rangeResult;
            try {
                VersionRangeRequest rangeRequest = new VersionRangeRequest(
                        root.getArtifact(), request.getRepositories(), request.getRequestContext());
                rangeRequest.setTrace(trace);
                rangeResult = versionRangeResolver.resolveVersionRange(session, rangeRequest);
                versions = filterVersions(root, rangeResult, verFilter, new DefaultVersionFilterContext(session));
            } catch (VersionRangeResolutionException e) {
                result.addException(e);
                throw new DependencyCollectionException(result, e.getMessage());
            }

            Version version = versions.get(versions.size() - 1);
            root = root.setArtifact(root.getArtifact().setVersion(version.toString()));

            ArtifactDescriptorResult descriptorResult;
            try {
                ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
                descriptorRequest.setArtifact(root.getArtifact());
                descriptorRequest.setRepositories(request.getRepositories());
                descriptorRequest.setRequestContext(request.getRequestContext());
                descriptorRequest.setTrace(trace);
                if (isLackingDescriptor(root.getArtifact())) {
                    descriptorResult = new ArtifactDescriptorResult(descriptorRequest);
                } else {
                    descriptorResult = descriptorReader.readArtifactDescriptor(session, descriptorRequest);
                }
            } catch (ArtifactDescriptorException e) {
                result.addException(e);
                throw new DependencyCollectionException(result, e.getMessage());
            }

            root = root.setArtifact(descriptorResult.getArtifact());

            if (!session.isIgnoreArtifactDescriptorRepositories()) {
                repositories = remoteRepositoryManager.aggregateRepositories(
                        session, repositories, descriptorResult.getRepositories(), true);
            }
            dependencies = mergeDeps(dependencies, descriptorResult.getDependencies());
            managedDependencies = mergeDeps(managedDependencies, descriptorResult.getManagedDependencies());

            node = new DefaultDependencyNode(root);
            node.setRequestContext(request.getRequestContext());
            node.setRelocations(descriptorResult.getRelocations());
            node.setVersionConstraint(rangeResult.getVersionConstraint());
            node.setVersion(version);
            node.setAliases(descriptorResult.getAliases());
            node.setRepositories(request.getRepositories());
        } else {
            node = new DefaultDependencyNode((Artifact) null);
            node.setRequestContext(request.getRequestContext());
            node.setRepositories(request.getRepositories());
        }

        result.setRoot(node);

        boolean traverse = root == null || depTraverser == null || depTraverser.traverseDependency(root);
        String errorPath = null;
        if (traverse && !dependencies.isEmpty()) {
            DataPool pool = new DataPool(session);

            DefaultDependencyCollectionContext context = new DefaultDependencyCollectionContext(
                    session, null, root, managedDependencies);

            DefaultVersionFilterContext versionContext = new DefaultVersionFilterContext(session);

            Results results = new Results(result, session);

            doCollectDependencies(
                    session,
                    trace,
                    pool,
                    context,
                    versionContext,
                    request,
                    node,
                    repositories,
                    dependencies,
                    managedDependencies,
                    results);

            errorPath = results.getErrorPath();
        }

        DependencyGraphTransformer transformer = session.getDependencyGraphTransformer();
        if (transformer != null) {
            try {
                DefaultDependencyGraphTransformationContext context =
                        new DefaultDependencyGraphTransformationContext(session);
                context.put(TransformationContextKeys.STATS, stats);
                result.setRoot(transformer.transformGraph(node, context));
            } catch (RepositoryException e) {
                result.addException(e);
            }
        }

        if (errorPath != null) {
            throw new DependencyCollectionException(result, "Failed to collect dependencies at " + errorPath);
        }
        if (!result.getExceptions().isEmpty()) {
            throw new DependencyCollectionException(result);
        }

        return result;
    }

    /**
     * Creates child {@link RequestTrace} instance from passed in {@link RequestTrace} and parameters by creating
     * {@link CollectStepDataImpl} instance out of passed in data. Caller must ensure that passed in parameters are
     * NOT affected by threading (or that there is no multi threading involved). In other words, the passed in values
     * should be immutable.
     *
     * @param trace   The current trace instance.
     * @param context The context from {@link CollectRequest#getRequestContext()}, never {@code null}.
     * @param path    List representing the path of dependency nodes, never {@code null}. Caller must ensure, that this
     *                list does not change during the lifetime of the requested {@link RequestTrace} instance. If it may
     *                change, simplest is to pass here a copy of used list.
     * @param node    Currently collected node, that collector came by following the passed in path.
     * @return A child request trance instance, never {@code null}.
     */
    protected RequestTrace collectStepTrace(
            RequestTrace trace, String context, List<DependencyNode> path, Dependency node) {
        return RequestTrace.newChild(trace, new CollectStepDataImpl(context, path, node));
    }

    @SuppressWarnings("checkstyle:parameternumber")
    protected abstract void doCollectDependencies(
            RepositorySystemSession session,
            RequestTrace trace,
            DataPool pool,
            DefaultDependencyCollectionContext context,
            DefaultVersionFilterContext versionContext,
            CollectRequest request,
            DependencyNode node,
            List<RemoteRepository> repositories,
            List<Dependency> dependencies,
            List<Dependency> managedDependencies,
            Results results);

    protected RepositorySystemSession optimizeSession(RepositorySystemSession session) {
        DefaultRepositorySystemSession optimized = new DefaultRepositorySystemSession(session);
        optimized.setArtifactTypeRegistry(CachingArtifactTypeRegistry.newInstance(session));
        return optimized;
    }

    protected List<Dependency> mergeDeps(List<Dependency> dominant, List<Dependency> recessive) {
        List<Dependency> result;
        if (dominant == null || dominant.isEmpty()) {
            result = recessive;
        } else if (recessive == null || recessive.isEmpty()) {
            result = dominant;
        } else {
            int initialCapacity = dominant.size() + recessive.size();
            result = new ArrayList<>(initialCapacity);
            Collection<String> ids = new HashSet<>(initialCapacity, 1.0f);
            for (Dependency dependency : dominant) {
                ids.add(getId(dependency.getArtifact()));
                result.add(dependency);
            }
            for (Dependency dependency : recessive) {
                if (!ids.contains(getId(dependency.getArtifact()))) {
                    result.add(dependency);
                }
            }
        }
        return result;
    }

    protected static String getId(Artifact a) {
        return a.getGroupId() + ':' + a.getArtifactId() + ':' + a.getClassifier() + ':' + a.getExtension();
    }

    @SuppressWarnings("checkstyle:parameternumber")
    protected static DefaultDependencyNode createDependencyNode(
            List<Artifact> relocations,
            PremanagedDependency preManaged,
            VersionRangeResult rangeResult,
            Version version,
            Dependency d,
            Collection<Artifact> aliases,
            List<RemoteRepository> repos,
            String requestContext) {
        DefaultDependencyNode child = new DefaultDependencyNode(d);
        preManaged.applyTo(child);
        child.setRelocations(relocations);
        child.setVersionConstraint(rangeResult.getVersionConstraint());
        child.setVersion(version);
        child.setAliases(aliases);
        child.setRepositories(repos);
        child.setRequestContext(requestContext);
        return child;
    }

    protected static DefaultDependencyNode createDependencyNode(
            List<Artifact> relocations,
            PremanagedDependency preManaged,
            VersionRangeResult rangeResult,
            Version version,
            Dependency d,
            ArtifactDescriptorResult descriptorResult,
            DependencyNode cycleNode) {
        DefaultDependencyNode child = createDependencyNode(
                relocations,
                preManaged,
                rangeResult,
                version,
                d,
                descriptorResult.getAliases(),
                cycleNode.getRepositories(),
                cycleNode.getRequestContext());
        child.setChildren(cycleNode.getChildren());
        return child;
    }

    protected static ArtifactDescriptorRequest createArtifactDescriptorRequest(
            String requestContext, RequestTrace requestTrace, List<RemoteRepository> repositories, Dependency d) {
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(d.getArtifact());
        descriptorRequest.setRepositories(repositories);
        descriptorRequest.setRequestContext(requestContext);
        descriptorRequest.setTrace(requestTrace);
        return descriptorRequest;
    }

    protected static VersionRangeRequest createVersionRangeRequest(
            String requestContext,
            RequestTrace requestTrace,
            List<RemoteRepository> repositories,
            Dependency dependency) {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(dependency.getArtifact());
        rangeRequest.setRepositories(repositories);
        rangeRequest.setRequestContext(requestContext);
        rangeRequest.setTrace(requestTrace);
        return rangeRequest;
    }

    protected VersionRangeResult cachedResolveRangeResult(
            VersionRangeRequest rangeRequest, DataPool pool, RepositorySystemSession session)
            throws VersionRangeResolutionException {
        Object key = pool.toKey(rangeRequest);
        VersionRangeResult rangeResult = pool.getConstraint(key, rangeRequest);
        if (rangeResult == null) {
            rangeResult = versionRangeResolver.resolveVersionRange(session, rangeRequest);
            pool.putConstraint(key, rangeResult);
        }
        return rangeResult;
    }

    protected static boolean isLackingDescriptor(Artifact artifact) {
        return artifact.getProperty(ArtifactProperties.LOCAL_PATH, null) != null;
    }

    protected static List<RemoteRepository> getRemoteRepositories(
            ArtifactRepository repository, List<RemoteRepository> repositories) {
        if (repository instanceof RemoteRepository) {
            return Collections.singletonList((RemoteRepository) repository);
        }
        if (repository != null) {
            return Collections.emptyList();
        }
        return repositories;
    }

    protected static List<? extends Version> filterVersions(
            Dependency dependency,
            VersionRangeResult rangeResult,
            VersionFilter verFilter,
            DefaultVersionFilterContext verContext)
            throws VersionRangeResolutionException {
        if (rangeResult.getVersions().isEmpty()) {
            throw new VersionRangeResolutionException(
                    rangeResult, "No versions available for " + dependency.getArtifact() + " within specified range");
        }

        List<? extends Version> versions;
        if (verFilter != null && rangeResult.getVersionConstraint().getRange() != null) {
            verContext.set(dependency, rangeResult);
            verFilter.filterVersions(verContext);
            versions = verContext.get();
            if (versions.isEmpty()) {
                throw new VersionRangeResolutionException(
                        rangeResult,
                        "No acceptable versions for " + dependency.getArtifact() + ": " + rangeResult.getVersions());
            }
        } else {
            versions = rangeResult.getVersions();
        }
        return versions;
    }

    /**
     * Helper class used during collection.
     */
    protected static class Results {

        private final CollectResult result;

        final int maxExceptions;

        final int maxCycles;

        String errorPath;

        public Results(CollectResult result, RepositorySystemSession session) {
            this.result = result;

            maxExceptions =
                    ConfigUtils.getInteger(session, CONFIG_PROP_MAX_EXCEPTIONS_DEFAULT, CONFIG_PROP_MAX_EXCEPTIONS);

            maxCycles = ConfigUtils.getInteger(session, CONFIG_PROP_MAX_CYCLES_DEFAULT, CONFIG_PROP_MAX_CYCLES);
        }

        public String getErrorPath() {
            return errorPath;
        }

        public void addException(Dependency dependency, Exception e, List<DependencyNode> nodes) {
            if (maxExceptions < 0 || result.getExceptions().size() < maxExceptions) {
                result.addException(e);
                if (errorPath == null) {
                    StringBuilder buffer = new StringBuilder(256);
                    for (DependencyNode node : nodes) {
                        if (buffer.length() > 0) {
                            buffer.append(" -> ");
                        }
                        Dependency dep = node.getDependency();
                        if (dep != null) {
                            buffer.append(dep.getArtifact());
                        }
                    }
                    if (buffer.length() > 0) {
                        buffer.append(" -> ");
                    }
                    buffer.append(dependency.getArtifact());
                    errorPath = buffer.toString();
                }
            }
        }
    }
}
