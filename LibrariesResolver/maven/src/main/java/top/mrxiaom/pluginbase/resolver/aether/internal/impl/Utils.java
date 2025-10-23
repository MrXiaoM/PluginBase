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

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact;
import top.mrxiaom.pluginbase.resolver.aether.impl.OfflineController;
import top.mrxiaom.pluginbase.resolver.aether.metadata.Metadata;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ResolutionErrorPolicy;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ResolutionErrorPolicyRequest;
import top.mrxiaom.pluginbase.resolver.aether.transfer.RepositoryOfflineException;

/**
 * Internal utility methods.
 */
final class Utils {

    public static int getPolicy(RepositorySystemSession session, Artifact artifact, RemoteRepository repository) {
        ResolutionErrorPolicy rep = session.getResolutionErrorPolicy();
        if (rep == null) {
            return ResolutionErrorPolicy.CACHE_DISABLED;
        }
        return rep.getArtifactPolicy(session, new ResolutionErrorPolicyRequest<>(artifact, repository));
    }

    public static int getPolicy(RepositorySystemSession session, Metadata metadata, RemoteRepository repository) {
        ResolutionErrorPolicy rep = session.getResolutionErrorPolicy();
        if (rep == null) {
            return ResolutionErrorPolicy.CACHE_DISABLED;
        }
        return rep.getMetadataPolicy(session, new ResolutionErrorPolicyRequest<>(metadata, repository));
    }

    public static void checkOffline(
            RepositorySystemSession session, OfflineController offlineController, RemoteRepository repository)
            throws RepositoryOfflineException {
        if (session.isOffline()) {
            offlineController.checkOffline(session, repository);
        }
    }
}
