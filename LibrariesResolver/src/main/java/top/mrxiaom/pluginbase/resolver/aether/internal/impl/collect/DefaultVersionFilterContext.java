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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.collection.VersionFilter;
import top.mrxiaom.pluginbase.resolver.aether.graph.Dependency;
import top.mrxiaom.pluginbase.resolver.aether.repository.ArtifactRepository;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.resolution.VersionRangeResult;
import top.mrxiaom.pluginbase.resolver.aether.version.Version;
import top.mrxiaom.pluginbase.resolver.aether.version.VersionConstraint;

/**
 * Default implementation of {@link VersionFilter.VersionFilterContext}.
 * Internal helper class for collector implementations.
 */
public final class DefaultVersionFilterContext implements VersionFilter.VersionFilterContext {
    private final RepositorySystemSession session;

    private Dependency dependency;

    VersionRangeResult result;

    private List<Version> versions;

    public DefaultVersionFilterContext(RepositorySystemSession session) {
        this.session = session;
    }

    public void set(Dependency dependency, VersionRangeResult result) {
        this.dependency = dependency;
        this.result = result;
        this.versions = new ArrayList<>(result.getVersions());
    }

    public List<Version> get() {
        return new ArrayList<>(versions);
    }

    @Override
    public RepositorySystemSession getSession() {
        return session;
    }

    @Override
    public Dependency getDependency() {
        return dependency;
    }

    @Override
    public VersionConstraint getVersionConstraint() {
        return result.getVersionConstraint();
    }

    @Override
    public int getCount() {
        return versions.size();
    }

    @Override
    public ArtifactRepository getRepository(Version version) {
        return result.getRepository(version);
    }

    @Override
    public List<RemoteRepository> getRepositories() {
        return Collections.unmodifiableList(result.getRequest().getRepositories());
    }

    @Override
    public @NotNull Iterator<Version> iterator() {
        return versions.iterator();
    }

    @Override
    public String toString() {
        return dependency + " " + result.getVersions();
    }
}
