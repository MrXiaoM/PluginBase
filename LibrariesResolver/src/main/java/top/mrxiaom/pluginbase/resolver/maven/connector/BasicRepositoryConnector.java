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
package top.mrxiaom.pluginbase.resolver.maven.connector;

import top.mrxiaom.pluginbase.resolver.aether.ConfigurationProperties;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.RequestTrace;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.spi.checksums.ProvidedChecksumsSource;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.ArtifactDownload;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.MetadataDownload;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.RepositoryConnector;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.checksum.ChecksumAlgorithmFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.checksum.ChecksumPolicy;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.checksum.ChecksumPolicyProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayout;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.layout.RepositoryLayoutProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.GetTask;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.PeekTask;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.Transporter;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.TransporterProvider;
import top.mrxiaom.pluginbase.resolver.aether.spi.io.FileProcessor;
import top.mrxiaom.pluginbase.resolver.aether.transfer.*;
import top.mrxiaom.pluginbase.resolver.aether.util.ConfigUtils;
import top.mrxiaom.pluginbase.resolver.aether.util.FileUtils;
import top.mrxiaom.pluginbase.resolver.aether.util.concurrency.ExecutorUtils;
import top.mrxiaom.pluginbase.resolver.aether.util.concurrency.RunnableErrorForwarder;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

final class BasicRepositoryConnector implements RepositoryConnector {

    private static final String CONFIG_PROP_THREADS = "aether.connector.basic.threads";

    private static final String CONFIG_PROP_SMART_CHECKSUMS = "aether.connector.smartChecksums";

    private final Map<String, ProvidedChecksumsSource> providedChecksumsSources;

    private final FileProcessor fileProcessor;

    private final RemoteRepository repository;

    private final RepositorySystemSession session;

    private final Transporter transporter;

    private final RepositoryLayout layout;

    private final ChecksumPolicyProvider checksumPolicyProvider;

    private final int maxThreads;

    private final boolean smartChecksums;

    private final boolean persistedChecksums;

    private Executor executor;

    private final AtomicBoolean closed;

    BasicRepositoryConnector(
            RepositorySystemSession session,
            RemoteRepository repository,
            TransporterProvider transporterProvider,
            RepositoryLayoutProvider layoutProvider,
            ChecksumPolicyProvider checksumPolicyProvider,
            FileProcessor fileProcessor,
            Map<String, ProvidedChecksumsSource> providedChecksumsSources)
            throws NoRepositoryConnectorException {
        try {
            layout = layoutProvider.newRepositoryLayout(session, repository);
        } catch (NoRepositoryLayoutException e) {
            throw new NoRepositoryConnectorException(repository, e.getMessage(), e);
        }
        try {
            transporter = transporterProvider.newTransporter(session, repository);
        } catch (NoTransporterException e) {
            throw new NoRepositoryConnectorException(repository, e.getMessage(), e);
        }
        this.checksumPolicyProvider = checksumPolicyProvider;

        this.session = session;
        this.repository = repository;
        this.fileProcessor = fileProcessor;
        this.providedChecksumsSources = providedChecksumsSources;
        this.closed = new AtomicBoolean(false);

        maxThreads = ExecutorUtils.threadCount(session, 5, CONFIG_PROP_THREADS, "maven.artifact.threads");
        smartChecksums = ConfigUtils.getBoolean(session, true, CONFIG_PROP_SMART_CHECKSUMS);
        persistedChecksums = ConfigUtils.getBoolean(
                session,
                ConfigurationProperties.DEFAULT_PERSISTED_CHECKSUMS,
                ConfigurationProperties.PERSISTED_CHECKSUMS);
    }

    private Executor getExecutor(int tasks) {
        if (maxThreads <= 1) {
            return ExecutorUtils.DIRECT_EXECUTOR;
        }
        if (tasks <= 1) {
            return ExecutorUtils.DIRECT_EXECUTOR;
        }
        if (executor == null) {
            executor =
                    ExecutorUtils.threadPool(maxThreads, getClass().getSimpleName() + '-' + repository.getHost() + '-');
        }
        return executor;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            ExecutorUtils.shutdown(executor);
            transporter.close();
        }
    }

    private void failIfClosed() {
        if (closed.get()) {
            throw new IllegalStateException("connector already closed");
        }
    }

    @Override
    public void get(
            Collection<? extends ArtifactDownload> artifactDownloads,
            Collection<? extends MetadataDownload> metadataDownloads) {
        failIfClosed();

        Collection<? extends ArtifactDownload> safeArtifactDownloads = safe(artifactDownloads);
        Collection<? extends MetadataDownload> safeMetadataDownloads = safe(metadataDownloads);

        Executor executor = getExecutor(safeArtifactDownloads.size() + safeMetadataDownloads.size());
        RunnableErrorForwarder errorForwarder = new RunnableErrorForwarder();
        List<ChecksumAlgorithmFactory> checksumAlgorithmFactories = layout.getChecksumAlgorithmFactories();

        boolean first = true;

        for (MetadataDownload transfer : safeMetadataDownloads) {
            URI location = layout.getLocation(transfer.getMetadata(), false);

            TransferResource resource = newTransferResource(location, transfer.getFile(), transfer.getTrace());
            TransferEvent.Builder builder = newEventBuilder(resource, false);
            MetadataTransportListener listener = new MetadataTransportListener(transfer, repository, builder);

            ChecksumPolicy checksumPolicy = newChecksumPolicy(transfer.getChecksumPolicy(), resource);
            List<RepositoryLayout.ChecksumLocation> checksumLocations = null;
            if (checksumPolicy != null) {
                checksumLocations = layout.getChecksumLocations(transfer.getMetadata(), false, location);
            }

            Runnable task = new GetTaskRunner(
                    location,
                    transfer.getFile(),
                    checksumPolicy,
                    checksumAlgorithmFactories,
                    checksumLocations,
                    null,
                    listener);
            if (first) {
                task.run();
                first = false;
            } else {
                executor.execute(errorForwarder.wrap(task));
            }
        }

        for (ArtifactDownload transfer : safeArtifactDownloads) {
            Map<String, String> providedChecksums = Collections.emptyMap();
            for (ProvidedChecksumsSource providedChecksumsSource : providedChecksumsSources.values()) {
                Map<String, String> provided = providedChecksumsSource.getProvidedArtifactChecksums(
                        session, transfer, repository, checksumAlgorithmFactories);

                if (provided != null) {
                    providedChecksums = provided;
                    break;
                }
            }

            URI location = layout.getLocation(transfer.getArtifact(), false);

            TransferResource resource = newTransferResource(location, transfer.getFile(), transfer.getTrace());
            TransferEvent.Builder builder = newEventBuilder(resource, transfer.isExistenceCheck());
            ArtifactTransportListener listener = new ArtifactTransportListener(transfer, repository, builder);

            Runnable task;
            if (transfer.isExistenceCheck()) {
                task = new PeekTaskRunner(location, listener);
            } else {
                ChecksumPolicy checksumPolicy = newChecksumPolicy(transfer.getChecksumPolicy(), resource);
                List<RepositoryLayout.ChecksumLocation> checksumLocations = null;
                if (checksumPolicy != null) {
                    checksumLocations = layout.getChecksumLocations(transfer.getArtifact(), false, location);
                }

                task = new GetTaskRunner(
                        location,
                        transfer.getFile(),
                        checksumPolicy,
                        checksumAlgorithmFactories,
                        checksumLocations,
                        providedChecksums,
                        listener);
            }
            if (first) {
                task.run();
                first = false;
            } else {
                executor.execute(errorForwarder.wrap(task));
            }
        }

        errorForwarder.await();
    }

    private static <T> Collection<T> safe(Collection<T> items) {
        return (items != null) ? items : Collections.emptyList();
    }

    private TransferResource newTransferResource(URI path, File file, RequestTrace trace) {
        return new TransferResource(repository.getId(), repository.getUrl(), path.toString(), file, trace);
    }

    private TransferEvent.Builder newEventBuilder(TransferResource resource, boolean peek) {
        TransferEvent.Builder builder = new TransferEvent.Builder(session, resource);
        if (!peek) {
            builder.setRequestType(TransferEvent.RequestType.GET);
        } else {
            builder.setRequestType(TransferEvent.RequestType.GET_EXISTENCE);
        }
        return builder;
    }

    private ChecksumPolicy newChecksumPolicy(String policy, TransferResource resource) {
        return checksumPolicyProvider.newChecksumPolicy(session, repository, resource, policy);
    }

    @Override
    public String toString() {
        return String.valueOf(repository);
    }

    abstract class TaskRunner implements Runnable {

        protected final URI path;

        protected final TransferTransportListener<?> listener;

        TaskRunner(URI path, TransferTransportListener<?> listener) {
            this.path = path;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                listener.transferInitiated();
                runTask();
                listener.transferSucceeded();
            } catch (Exception e) {
                listener.transferFailed(e, transporter.classify(e));
            }
        }

        protected abstract void runTask() throws Exception;
    }

    class PeekTaskRunner extends TaskRunner {

        PeekTaskRunner(URI path, TransferTransportListener<?> listener) {
            super(path, listener);
        }

        @Override
        protected void runTask() throws Exception {
            transporter.peek(new PeekTask(path));
        }
    }

    class GetTaskRunner extends TaskRunner implements ChecksumValidator.ChecksumFetcher {

        private final File file;

        private final ChecksumValidator checksumValidator;

        GetTaskRunner(
                URI path,
                File file,
                ChecksumPolicy checksumPolicy,
                List<ChecksumAlgorithmFactory> checksumAlgorithmFactories,
                List<RepositoryLayout.ChecksumLocation> checksumLocations,
                Map<String, String> providedChecksums,
                TransferTransportListener<?> listener) {
            super(path, listener);
            this.file = requireNonNull(file, "destination file cannot be null");
            checksumValidator = new ChecksumValidator(
                    file,
                    checksumAlgorithmFactories,
                    fileProcessor,
                    this,
                    checksumPolicy,
                    providedChecksums,
                    safe(checksumLocations));
        }

        @Override
        public boolean fetchChecksum(URI remote, File local) throws Exception {
            try {
                transporter.get(new GetTask(remote).setDataFile(local));
            } catch (Exception e) {
                if (transporter.classify(e) == Transporter.ERROR_NOT_FOUND) {
                    return false;
                }
                throw e;
            }
            return true;
        }

        @Override
        protected void runTask() throws Exception {
            try (FileUtils.CollocatedTempFile tempFile = FileUtils.newTempFile(file.toPath())) {
                final File tmp = tempFile.getPath().toFile();
                listener.setChecksumCalculator(checksumValidator.newChecksumCalculator(tmp));
                for (int firstTrial = 0, lastTrial = 1, trial = firstTrial; ; trial++) {
                    GetTask task = new GetTask(path).setDataFile(tmp, false).setListener(listener);
                    transporter.get(task);
                    try {
                        checksumValidator.validate(
                                listener.getChecksums(), smartChecksums ? task.getChecksums() : null);
                        break;
                    } catch (ChecksumFailureException e) {
                        boolean retry = trial < lastTrial && e.isRetryWorthy();
                        if (!retry && !checksumValidator.handle(e)) {
                            throw e;
                        }
                        listener.transferCorrupted(e);
                        if (retry) {
                            checksumValidator.retry();
                        } else {
                            break;
                        }
                    }
                }
                tempFile.move();
                if (persistedChecksums) {
                    checksumValidator.commit();
                }
            }
        }
    }
}
