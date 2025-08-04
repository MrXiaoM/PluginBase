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
package top.mrxiaom.pluginbase.resolver.aether.util.graph.manager;

import top.mrxiaom.pluginbase.resolver.aether.graph.DependencyNode;

/**
 * A utility class assisting in analyzing the effects of dependency management.
 */
public final class DependencyManagerUtils {

    /**
     * The key in the repository session's {@link top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession#getConfigProperties()
     * configuration properties} used to store a {@link Boolean} flag controlling the verbose mode for dependency
     * management. If enabled, the original attributes of a dependency before its update due to dependency managemnent
     * will be recorded * in the node's {@link DependencyNode#getData() custom data} when building a dependency graph.
     */
    public static final String CONFIG_PROP_VERBOSE = "aether.dependencyManager.verbose";

    /**
     * The key in the dependency node's {@link DependencyNode#getData() custom data} under which the original version is
     * stored.
     */
    public static final String NODE_DATA_PREMANAGED_VERSION = "premanaged.version";

    /**
     * The key in the dependency node's {@link DependencyNode#getData() custom data} under which the original scope is
     * stored.
     */
    public static final String NODE_DATA_PREMANAGED_SCOPE = "premanaged.scope";

    /**
     * The key in the dependency node's {@link DependencyNode#getData() custom data} under which the original optional
     * flag is stored.
     */
    public static final String NODE_DATA_PREMANAGED_OPTIONAL = "premanaged.optional";

    /**
     * The key in the dependency node's {@link DependencyNode#getData() custom data} under which the original exclusions
     * are stored.
     *
     * @since 1.1.0
     */
    public static final String NODE_DATA_PREMANAGED_EXCLUSIONS = "premanaged.exclusions";

    /**
     * The key in the dependency node's {@link DependencyNode#getData() custom data} under which the original properties
     * are stored.
     *
     * @since 1.1.0
     */
    public static final String NODE_DATA_PREMANAGED_PROPERTIES = "premanaged.properties";
}
