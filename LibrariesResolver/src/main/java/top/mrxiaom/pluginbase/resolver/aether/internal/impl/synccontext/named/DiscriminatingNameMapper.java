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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl.synccontext.named;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact;
import top.mrxiaom.pluginbase.resolver.aether.metadata.Metadata;
import top.mrxiaom.pluginbase.resolver.aether.util.ConfigUtils;
import top.mrxiaom.pluginbase.resolver.aether.util.StringDigestUtil;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Wrapping {@link NameMapper}, that wraps another {@link NameMapper} and adds a "discriminator" as prefix, that
 * makes lock names unique including the hostname and local repository (by default). The discriminator may be passed
 * in via {@link RepositorySystemSession} or is automatically calculated based on the local hostname and repository
 * path. The implementation retains order of collection elements as it got it from
 * {@link NameMapper#nameLocks(RepositorySystemSession, Collection, Collection)} method.
 * <p>
 * The default setup wraps {@link GAVNameMapper}, but manually may be created any instance needed.
 */
public class DiscriminatingNameMapper implements NameMapper {
    /**
     * Configuration property to pass in discriminator
     */
    private static final String CONFIG_PROP_DISCRIMINATOR = "aether.syncContext.named.discriminating.discriminator";

    /**
     * Configuration property to pass in hostname
     */
    private static final String CONFIG_PROP_HOSTNAME = "aether.syncContext.named.discriminating.hostname";

    private static final String DEFAULT_DISCRIMINATOR_DIGEST = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

    private static final String DEFAULT_HOSTNAME = "localhost";

    private final NameMapper delegate;

    private final String hostname;

    public DiscriminatingNameMapper(final NameMapper delegate) {
        this.delegate = requireNonNull(delegate);
        this.hostname = getHostname();
    }

    @Override
    public boolean isFileSystemFriendly() {
        return false; // uses ":" in produced lock names
    }

    @Override
    public Collection<String> nameLocks(
            final RepositorySystemSession session,
            final Collection<? extends Artifact> artifacts,
            final Collection<? extends Metadata> metadatas) {
        String discriminator = createDiscriminator(session);
        return delegate.nameLocks(session, artifacts, metadatas).stream()
                .map(s -> discriminator + ":" + s)
                .collect(toList());
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return DEFAULT_HOSTNAME;
        }
    }

    private String createDiscriminator(final RepositorySystemSession session) {
        String discriminator = ConfigUtils.getString(session, null, CONFIG_PROP_DISCRIMINATOR);

        if (discriminator == null || discriminator.isEmpty()) {
            String hostname = ConfigUtils.getString(session, this.hostname, CONFIG_PROP_HOSTNAME);
            File basedir = session.getLocalRepository().getBasedir();
            discriminator = hostname + ":" + basedir;
            try {
                return StringDigestUtil.sha1(discriminator);
            } catch (Exception e) {
                return DEFAULT_DISCRIMINATOR_DIGEST;
            }
        }
        return discriminator;
    }
}
