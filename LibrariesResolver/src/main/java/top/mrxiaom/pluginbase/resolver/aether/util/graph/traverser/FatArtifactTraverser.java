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
package top.mrxiaom.pluginbase.resolver.aether.util.graph.traverser;

import top.mrxiaom.pluginbase.resolver.aether.artifact.ArtifactProperties;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyCollectionContext;
import top.mrxiaom.pluginbase.resolver.aether.collection.DependencyTraverser;
import top.mrxiaom.pluginbase.resolver.aether.graph.Dependency;

import static java.util.Objects.requireNonNull;

/**
 * A dependency traverser that excludes the dependencies of fat artifacts from the traversal. Fat artifacts are
 * artifacts that have the property {@link top.mrxiaom.pluginbase.resolver.aether.artifact.ArtifactProperties#INCLUDES_DEPENDENCIES} set to
 * {@code true}.
 *
 * @see top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact#getProperties()
 */
public final class FatArtifactTraverser implements DependencyTraverser {

    /**
     * Creates a new instance of this dependency traverser.
     */
    public FatArtifactTraverser() {}

    public boolean traverseDependency(Dependency dependency) {
        requireNonNull(dependency, "dependency cannot be null");
        String prop = dependency.getArtifact().getProperty(ArtifactProperties.INCLUDES_DEPENDENCIES, "");
        return !Boolean.parseBoolean(prop);
    }

    public DependencyTraverser deriveChildTraverser(DependencyCollectionContext context) {
        requireNonNull(context, "context cannot be null");
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else return null != obj && getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
