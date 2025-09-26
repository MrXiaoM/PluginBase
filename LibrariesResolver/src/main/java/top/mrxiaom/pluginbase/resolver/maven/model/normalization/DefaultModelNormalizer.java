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
package top.mrxiaom.pluginbase.resolver.maven.model.normalization;

import top.mrxiaom.pluginbase.resolver.plexus.util.StringUtils;
import top.mrxiaom.pluginbase.resolver.maven.model.Build;
import top.mrxiaom.pluginbase.resolver.maven.model.Dependency;
import top.mrxiaom.pluginbase.resolver.maven.model.Model;
import top.mrxiaom.pluginbase.resolver.maven.model.Plugin;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelBuildingRequest;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblemCollector;
import top.mrxiaom.pluginbase.resolver.maven.model.merge.MavenModelMerger;

import java.util.*;

/**
 * Handles normalization of a model.
 *
 * @author Benjamin Bentmann
 */
public class DefaultModelNormalizer implements ModelNormalizer {

    private DuplicateMerger merger = new DuplicateMerger();

    @Override
    public void mergeDuplicates(Model model, ModelBuildingRequest request, ModelProblemCollector problems) {
        Build build = model.getBuild();
        if (build != null) {
            List<Plugin> plugins = build.getPlugins();
            Map<Object, Plugin> normalized = new LinkedHashMap<>(plugins.size() * 2);

            for (Plugin plugin : plugins) {
                Object key = plugin.getKey();
                Plugin first = normalized.get(key);
                if (first != null) {
                    merger.mergePlugin(plugin, first);
                }
                normalized.put(key, plugin);
            }

            if (plugins.size() != normalized.size()) {
                build.setPlugins(new ArrayList<>(normalized.values()));
            }
        }

        /*
         * NOTE: This is primarily to keep backward-compat with Maven 2.x which did not validate that dependencies are
         * unique within a single POM. Upon multiple declarations, 2.x just kept the last one but retained the order of
         * the first occurrence. So when we're in lenient/compat mode, we have to deal with such broken POMs and mimic
         * the way 2.x works. When we're in strict mode, the removal of duplicates just saves other merging steps from
         * aftereffects and bogus error messages.
         */
        List<Dependency> dependencies = model.getDependencies();
        Map<String, Dependency> normalized = new LinkedHashMap<>(dependencies.size() * 2);

        for (Dependency dependency : dependencies) {
            normalized.put(dependency.getManagementKey(), dependency);
        }

        if (dependencies.size() != normalized.size()) {
            model.setDependencies(new ArrayList<>(normalized.values()));
        }
    }

    /**
     * DuplicateMerger
     */
    protected static class DuplicateMerger extends MavenModelMerger {

        public void mergePlugin(Plugin target, Plugin source) {
            super.mergePlugin(target, source, false, Collections.emptyMap());
        }
    }

    @Override
    public void injectDefaultValues(Model model, ModelBuildingRequest request, ModelProblemCollector problems) {
        injectDependencyDefaults(model.getDependencies());

        Build build = model.getBuild();
        if (build != null) {
            for (Plugin plugin : build.getPlugins()) {
                injectDependencyDefaults(plugin.getDependencies());
            }
        }
    }

    private void injectDependencyDefaults(List<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            if (StringUtils.isEmpty(dependency.getScope())) {
                // we cannot set this directly in the MDO due to the interactions with dependency management
                dependency.setScope("compile");
            }
        }
    }
}
