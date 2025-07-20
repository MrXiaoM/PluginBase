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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.ArtifactDownload;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.MetadataDownload;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.RepositoryConnector;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.filter.RemoteRepositoryFilter;
import top.mrxiaom.pluginbase.resolver.aether.transfer.ArtifactNotFoundException;
import top.mrxiaom.pluginbase.resolver.aether.transfer.MetadataNotFoundException;

import static java.util.Objects.requireNonNull;

/**
 * A filtering connector that filter transfers using remote repository filter and delegates to another connector.
 *
 * @since 1.9.0
 */
public final class FilteringRepositoryConnector implements RepositoryConnector {
    private final RemoteRepository remoteRepository;

    private final RepositoryConnector delegate;

    private final RemoteRepositoryFilter remoteRepositoryFilter;

    public FilteringRepositoryConnector(
            RemoteRepository remoteRepository,
            RepositoryConnector delegate,
            RemoteRepositoryFilter remoteRepositoryFilter) {
        this.remoteRepository = requireNonNull(remoteRepository);
        this.delegate = requireNonNull(delegate);
        this.remoteRepositoryFilter = requireNonNull(remoteRepositoryFilter);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void get(
            Collection<? extends ArtifactDownload> artifactDownloads,
            Collection<? extends MetadataDownload> metadataDownloads) {
        List<ArtifactDownload> filteredArtifactDownloads = null;
        if (artifactDownloads != null && !artifactDownloads.isEmpty()) {
            filteredArtifactDownloads = new ArrayList<>(artifactDownloads.size());
            for (ArtifactDownload artifactDownload : artifactDownloads) {
                RemoteRepositoryFilter.Result result =
                        remoteRepositoryFilter.acceptArtifact(remoteRepository, artifactDownload.getArtifact());
                if (result.isAccepted()) {
                    filteredArtifactDownloads.add(artifactDownload);
                } else {
                    artifactDownload.setException(new ArtifactNotFoundException(
                            artifactDownload.getArtifact(), remoteRepository, result.reasoning()));
                }
            }
        }
        List<MetadataDownload> filteredMetadataDownloads = null;
        if (metadataDownloads != null && !metadataDownloads.isEmpty()) {
            filteredMetadataDownloads = new ArrayList<>(metadataDownloads.size());
            for (MetadataDownload metadataDownload : metadataDownloads) {
                RemoteRepositoryFilter.Result result =
                        remoteRepositoryFilter.acceptMetadata(remoteRepository, metadataDownload.getMetadata());
                if (result.isAccepted()) {
                    filteredMetadataDownloads.add(metadataDownload);
                } else {
                    metadataDownload.setException(new MetadataNotFoundException(
                            metadataDownload.getMetadata(), remoteRepository, result.reasoning()));
                }
            }
        }
        delegate.get(filteredArtifactDownloads, filteredMetadataDownloads);
    }

    @Override
    public String toString() {
        return "filtered(" + delegate.toString() + ")";
    }
}
