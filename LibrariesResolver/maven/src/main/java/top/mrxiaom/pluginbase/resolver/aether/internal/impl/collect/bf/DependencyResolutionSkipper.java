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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl.collect.bf;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact;
import top.mrxiaom.pluginbase.resolver.aether.graph.DependencyNode;
import top.mrxiaom.pluginbase.resolver.aether.util.artifact.ArtifactIdUtils;

/**
 * A skipper that determines whether to skip resolving given node during the dependency collection.
 * Internal helper for {@link BfDependencyCollector}.
 *
 * @since 1.8.0
 */
abstract class DependencyResolutionSkipper implements Closeable {
    /**
     * Check whether the resolution of current node can be skipped before resolving.
     *
     * @param node    Current node
     * @param parents All parent nodes of current node
     *
     * @return {@code true} if the node can be skipped for resolution, {@code false} if resolution required.
     */
    abstract boolean skipResolution(DependencyNode node, List<DependencyNode> parents);

    /**
     * Cache the resolution result when a node is resolved by {@link BfDependencyCollector) after resolution.
     * <p>
     * @param node    Current node
     * @param parents All parent nodes of current node
     */
    abstract void cache(DependencyNode node, List<DependencyNode> parents);

    /**
     * Close: Print the skip/resolve status report for all nodes.
     */
    @Override
    public abstract void close();

    /**
     * Returns new instance of "default" skipper.
     * <p>
     * Note: type is specialized for testing purposes.
     */
    public static DefaultDependencyResolutionSkipper defaultSkipper() {
        return new DefaultDependencyResolutionSkipper();
    }

    /**
     * Returns instance of "never" skipper.
     */
    public static DependencyResolutionSkipper neverSkipper() {
        return NeverDependencyResolutionSkipper.INSTANCE;
    }

    /**
     * NEVER implementation.
     */
    private static final class NeverDependencyResolutionSkipper extends DependencyResolutionSkipper {
        private static final DependencyResolutionSkipper INSTANCE = new NeverDependencyResolutionSkipper();

        @Override
        public boolean skipResolution(DependencyNode node, List<DependencyNode> parents) {
            return false;
        }

        @Override
        public void cache(DependencyNode node, List<DependencyNode> parents) {}

        @Override
        public void close() {}
    }

    /**
     * Visible for testing.
     */
    static final class DefaultDependencyResolutionSkipper extends DependencyResolutionSkipper {
        private final Map<DependencyNode, DependencyResolutionResult> results = new LinkedHashMap<>(256);
        private final CacheManager cacheManager = new CacheManager();
        private final CoordinateManager coordinateManager = new CoordinateManager();

        @Override
        public boolean skipResolution(DependencyNode node, List<DependencyNode> parents) {
            DependencyResolutionResult result = new DependencyResolutionResult(node);
            results.put(node, result);

            int depth = parents.size() + 1;
            coordinateManager.createCoordinate(node, depth);

            if (cacheManager.isVersionConflict(node)) {
                /*
                 * Skip resolving version conflict losers (omitted for conflict)
                 */
                result.skippedAsVersionConflict = true;
            } else if (cacheManager.isDuplicate(node)) {
                if (coordinateManager.isLeftmost(node, parents)) {
                    /*
                     * Force resolving the node to retain conflict paths when its coordinate is
                     * more left than last resolved
                     * This is because Maven picks the widest scope present among conflicting dependencies
                     */
                    result.forceResolution = true;
                } else {
                    /*
                     * Skip resolving as duplicate (depth deeper, omitted for duplicate)
                     * No need to compare depth as the depth of winner for given artifact is always shallower
                     */
                    result.skippedAsDuplicate = true;
                }
            } else {
                result.resolve = true;
            }

            if (result.toResolve()) {
                coordinateManager.updateLeftmost(node);
                return false;
            }

            return true;
        }

        @Override
        public void cache(DependencyNode node, List<DependencyNode> parents) {
            boolean parentForceResolution =
                    parents.stream().anyMatch(n -> results.containsKey(n) && results.get(n).forceResolution);
            if (!parentForceResolution) {
                cacheManager.cacheWinner(node);
            }
        }

        @Override
        public void close() {
        }

        public Map<DependencyNode, DependencyResolutionResult> getResults() {
            return results;
        }

        private static final class CacheManager {

            /**
             * artifact -> node
             */
            private final Map<Artifact, DependencyNode> winners = new HashMap<>(256);

            /**
             * versionLessId -> Artifact, only cache winners
             */
            private final Map<String, Artifact> winnerGAs = new HashMap<>(256);

            boolean isVersionConflict(DependencyNode node) {
                String ga = ArtifactIdUtils.toVersionlessId(node.getArtifact());
                if (winnerGAs.containsKey(ga)) {
                    Artifact result = winnerGAs.get(ga);
                    return !node.getArtifact().getVersion().equals(result.getVersion());
                }

                return false;
            }

            void cacheWinner(DependencyNode node) {
                winners.put(node.getArtifact(), node);
                winnerGAs.put(ArtifactIdUtils.toVersionlessId(node.getArtifact()), node.getArtifact());
            }

            boolean isDuplicate(DependencyNode node) {
                return winners.containsKey(node.getArtifact());
            }
        }

        private static final class CoordinateManager {
            private final Map<Integer, AtomicInteger> sequenceGen = new HashMap<>(256);

            /**
             * Dependency node -> Coordinate
             */
            private final Map<DependencyNode, Coordinate> coordinateMap = new HashMap<>(256);

            /**
             * Leftmost coordinate of given artifact
             */
            private final Map<Artifact, Coordinate> leftmostCoordinates = new HashMap<>(256);

            Coordinate getCoordinate(DependencyNode node) {
                return coordinateMap.get(node);
            }

            Coordinate createCoordinate(DependencyNode node, int depth) {
                int seq = sequenceGen
                        .computeIfAbsent(depth, k -> new AtomicInteger())
                        .incrementAndGet();
                Coordinate coordinate = new Coordinate(depth, seq);
                coordinateMap.put(node, coordinate);
                return coordinate;
            }

            void updateLeftmost(DependencyNode current) {
                leftmostCoordinates.put(current.getArtifact(), getCoordinate(current));
            }

            boolean isLeftmost(DependencyNode node, List<DependencyNode> parents) {
                Coordinate leftmost = leftmostCoordinates.get(node.getArtifact());
                if (leftmost != null && leftmost.depth <= parents.size()) {
                    DependencyNode sameLevelNode = parents.get(leftmost.depth - 1);
                    return getCoordinate(sameLevelNode).sequence < leftmost.sequence;
                }

                return false;
            }
        }

        private static final class Coordinate {
            int depth;
            int sequence;

            Coordinate(int depth, int sequence) {
                this.depth = depth;
                this.sequence = sequence;
            }

            @Override
            public String toString() {
                return "{" + "depth=" + depth + ", sequence=" + sequence + '}';
            }
        }
    }

    /**
     * Visible for testing.
     */
    static final class DependencyResolutionResult {
        DependencyNode current;
        boolean skippedAsVersionConflict; // omitted for conflict
        boolean skippedAsDuplicate; // omitted for duplicate, depth is deeper
        boolean resolve; // node to resolve (winner node)
        boolean forceResolution; // force resolving (duplicate node) for scope selection

        DependencyResolutionResult(DependencyNode current) {
            this.current = current;
        }

        boolean toResolve() {
            return resolve || forceResolution;
        }
    }
}
