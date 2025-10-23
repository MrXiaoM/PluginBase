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
package top.mrxiaom.pluginbase.resolver.maven.provider;

import top.mrxiaom.pluginbase.resolver.aether.DefaultRepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.artifact.DefaultArtifactType;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyGraphTransformer;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyManager;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencySelector;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyTraverser;
import top.mrxiaom.pluginbase.resolver.aether.impl.ArtifactDescriptorReader;
import top.mrxiaom.pluginbase.resolver.aether.impl.DefaultServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.impl.VersionRangeResolver;
import top.mrxiaom.pluginbase.resolver.aether.impl.VersionResolver;
import top.mrxiaom.pluginbase.resolver.aether.util.artifact.DefaultArtifactTypeRegistry;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.manager.ClassicDependencyManager;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.selector.AndDependencySelector;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.selector.ExclusionDependencySelector;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.selector.OptionalDependencySelector;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.selector.ScopeDependencySelector;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.transformer.*;
import top.mrxiaom.pluginbase.resolver.aether.util.graph.traverser.FatArtifactTraverser;
import top.mrxiaom.pluginbase.resolver.aether.util.repository.SimpleArtifactDescriptorPolicy;

/**
 * A utility class to assist in setting up a Maven-like repository system. <em>Note:</em> This component is meant to
 * assist those clients that employ the repository system outside of an IoC container, Maven plugins should instead
 * always use regular dependency injection to acquire the repository system.
 *
 * @author Benjamin Bentmann
 */
public final class MavenRepositorySystemUtils {

    private MavenRepositorySystemUtils() {
        // hide constructor
    }

    /**
     * Creates a new service locator that already knows about all service implementations included in this library. To
     * acquire a complete repository system, clients need to add some repository connectors for remote transfers.
     *
     * @return The new service locator, never {@code null}.
     */
    public static DefaultServiceLocator newServiceLocator() {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
        locator.addService(ModelCacheFactory.class, DefaultModelCacheFactory.class);
        return locator;
    }

    /**
     * Creates a new Maven-like repository system session by initializing the session with values typical for
     * Maven-based resolution. In more detail, this method configures settings relevant for the processing of dependency
     * graphs, most other settings remain at their generic default value. Use the various setters to further configure
     * the session with authentication, mirror, proxy and other information required for your environment.
     *
     * @return The new repository system session, never {@code null}.
     */
    public static DefaultRepositorySystemSession newSession() {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        DependencyTraverser depTraverser = new FatArtifactTraverser();
        session.setDependencyTraverser(depTraverser);

        DependencyManager depManager = new ClassicDependencyManager();
        session.setDependencyManager(depManager);

        DependencySelector depFilter = new AndDependencySelector(
                new ScopeDependencySelector("test", "provided"),
                new OptionalDependencySelector(),
                new ExclusionDependencySelector());
        session.setDependencySelector(depFilter);

        DependencyGraphTransformer transformer = new ConflictResolver(
                new NearestVersionSelector(), new JavaScopeSelector(),
                new SimpleOptionalitySelector(), new JavaScopeDeriver());
        transformer = new ChainedDependencyGraphTransformer(transformer, new JavaDependencyContextRefiner());
        session.setDependencyGraphTransformer(transformer);

        DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
        stereotypes.add(new DefaultArtifactType("pom"));
        stereotypes.add(new DefaultArtifactType("maven-plugin", "jar", "", "java"));
        stereotypes.add(new DefaultArtifactType("jar", "jar", "", "java"));
        stereotypes.add(new DefaultArtifactType("ejb", "jar", "", "java"));
        stereotypes.add(new DefaultArtifactType("ejb-client", "jar", "client", "java"));
        stereotypes.add(new DefaultArtifactType("test-jar", "jar", "tests", "java"));
        stereotypes.add(new DefaultArtifactType("javadoc", "jar", "javadoc", "java"));
        stereotypes.add(new DefaultArtifactType("java-source", "jar", "sources", "java", false, false));
        stereotypes.add(new DefaultArtifactType("war", "war", "", "java", false, true));
        stereotypes.add(new DefaultArtifactType("ear", "ear", "", "java", false, true));
        stereotypes.add(new DefaultArtifactType("rar", "rar", "", "java", false, true));
        stereotypes.add(new DefaultArtifactType("par", "par", "", "java", false, true));
        session.setArtifactTypeRegistry(stereotypes);

        session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true));

        return session;
    }
}
