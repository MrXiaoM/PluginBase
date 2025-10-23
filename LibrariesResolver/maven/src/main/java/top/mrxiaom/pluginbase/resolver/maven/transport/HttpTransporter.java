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
package top.mrxiaom.pluginbase.resolver.maven.transport;

import top.mrxiaom.pluginbase.resolver.http.*;
import top.mrxiaom.pluginbase.resolver.http.client.HttpRequestRetryHandler;
import top.mrxiaom.pluginbase.resolver.http.client.HttpResponseException;
import top.mrxiaom.pluginbase.resolver.http.client.ServiceUnavailableRetryStrategy;
import top.mrxiaom.pluginbase.resolver.http.client.config.RequestConfig;
import top.mrxiaom.pluginbase.resolver.http.client.methods.CloseableHttpResponse;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpGet;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpHead;
import top.mrxiaom.pluginbase.resolver.http.client.methods.HttpUriRequest;
import top.mrxiaom.pluginbase.resolver.http.client.utils.DateUtils;
import top.mrxiaom.pluginbase.resolver.http.client.utils.URIUtils;
import top.mrxiaom.pluginbase.resolver.http.config.SocketConfig;
import top.mrxiaom.pluginbase.resolver.http.entity.ByteArrayEntity;
import top.mrxiaom.pluginbase.resolver.http.impl.NoConnectionReuseStrategy;
import top.mrxiaom.pluginbase.resolver.http.impl.client.CloseableHttpClient;
import top.mrxiaom.pluginbase.resolver.http.impl.client.DefaultHttpRequestRetryHandler;
import top.mrxiaom.pluginbase.resolver.http.impl.client.HttpClientBuilder;
import top.mrxiaom.pluginbase.resolver.http.impl.client.StandardHttpRequestRetryHandler;
import top.mrxiaom.pluginbase.resolver.http.protocol.HttpContext;
import top.mrxiaom.pluginbase.resolver.http.util.EntityUtils;
import top.mrxiaom.pluginbase.resolver.aether.ConfigurationProperties;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.repository.Proxy;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.AbstractTransporter;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.GetTask;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.PeekTask;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.TransportTask;
import top.mrxiaom.pluginbase.resolver.aether.transfer.NoTransporterException;
import top.mrxiaom.pluginbase.resolver.aether.transfer.TransferCancelledException;
import top.mrxiaom.pluginbase.resolver.aether.util.ConfigUtils;
import top.mrxiaom.pluginbase.resolver.aether.util.FileUtils;
import top.mrxiaom.pluginbase.resolver.maven.transport.checksum.ChecksumExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A transporter for HTTP/HTTPS.
 */
final class HttpTransporter extends AbstractTransporter {

    static final String HTTP_RETRY_HANDLER_NAME = "aether.connector.http.retryHandler.name";

    private static final String HTTP_RETRY_HANDLER_NAME_STANDARD = "standard";

    private static final String HTTP_RETRY_HANDLER_NAME_DEFAULT = "default";

    private static final Pattern CONTENT_RANGE_PATTERN =
            Pattern.compile("\\s*bytes\\s+([0-9]+)\\s*-\\s*([0-9]+)\\s*/.*");

    private final Map<String, ChecksumExtractor> checksumExtractors;

    private final URI baseUri;

    private final HttpHost server;

    private final CloseableHttpClient client;

    private final Map<?, ?> headers;

    private final LocalState state;

    @SuppressWarnings("checkstyle:methodlength")
    HttpTransporter(
            Map<String, ChecksumExtractor> checksumExtractors,
            RemoteRepository repository,
            RepositorySystemSession session)
            throws NoTransporterException {
        if (!"http".equalsIgnoreCase(repository.getProtocol()) && !"https".equalsIgnoreCase(repository.getProtocol())) {
            throw new NoTransporterException(repository);
        }
        this.checksumExtractors = requireNonNull(checksumExtractors, "checksum extractors must not be null");
        try {
            this.baseUri = new URI(repository.getUrl()).parseServerAuthority();
            if (baseUri.isOpaque()) {
                throw new URISyntaxException(repository.getUrl(), "URL must not be opaque");
            }
            this.server = URIUtils.extractHost(baseUri);
            if (server == null) {
                throw new URISyntaxException(repository.getUrl(), "URL lacks host name");
            }
        } catch (URISyntaxException e) {
            throw new NoTransporterException(repository, e.getMessage(), e);
        }
        HttpHost proxy = toHost(repository.getProxy());

        String httpsSecurityMode = ConfigUtils.getString(
                session,
                ConfigurationProperties.HTTPS_SECURITY_MODE_DEFAULT,
                ConfigurationProperties.HTTPS_SECURITY_MODE + "." + repository.getId(),
                ConfigurationProperties.HTTPS_SECURITY_MODE);
        final int connectionMaxTtlSeconds = ConfigUtils.getInteger(
                session,
                ConfigurationProperties.DEFAULT_HTTP_CONNECTION_MAX_TTL,
                ConfigurationProperties.HTTP_CONNECTION_MAX_TTL + "." + repository.getId(),
                ConfigurationProperties.HTTP_CONNECTION_MAX_TTL);
        final int maxConnectionsPerRoute = ConfigUtils.getInteger(
                session,
                ConfigurationProperties.DEFAULT_HTTP_MAX_CONNECTIONS_PER_ROUTE,
                ConfigurationProperties.HTTP_MAX_CONNECTIONS_PER_ROUTE + "." + repository.getId(),
                ConfigurationProperties.HTTP_MAX_CONNECTIONS_PER_ROUTE);
        this.state = new LocalState(
                session,
                repository,
                new ConnMgrConfig(session, httpsSecurityMode, connectionMaxTtlSeconds, maxConnectionsPerRoute));

        this.headers = ConfigUtils.getMap(
                session,
                Collections.emptyMap(),
                ConfigurationProperties.HTTP_HEADERS + "." + repository.getId(),
                ConfigurationProperties.HTTP_HEADERS);

        int connectTimeout = ConfigUtils.getInteger(
                session,
                ConfigurationProperties.DEFAULT_CONNECT_TIMEOUT,
                ConfigurationProperties.CONNECT_TIMEOUT + "." + repository.getId(),
                ConfigurationProperties.CONNECT_TIMEOUT);
        int requestTimeout = ConfigUtils.getInteger(
                session,
                ConfigurationProperties.DEFAULT_REQUEST_TIMEOUT,
                ConfigurationProperties.REQUEST_TIMEOUT + "." + repository.getId(),
                ConfigurationProperties.REQUEST_TIMEOUT);
        int retryCount = ConfigUtils.getInteger(
                session,
                ConfigurationProperties.DEFAULT_HTTP_RETRY_HANDLER_COUNT,
                ConfigurationProperties.HTTP_RETRY_HANDLER_COUNT + "." + repository.getId(),
                ConfigurationProperties.HTTP_RETRY_HANDLER_COUNT);
        long retryInterval = ConfigUtils.getLong(
                session,
                ConfigurationProperties.DEFAULT_HTTP_RETRY_HANDLER_INTERVAL,
                ConfigurationProperties.HTTP_RETRY_HANDLER_INTERVAL + "." + repository.getId(),
                ConfigurationProperties.HTTP_RETRY_HANDLER_INTERVAL);
        long retryIntervalMax = ConfigUtils.getLong(
                session,
                ConfigurationProperties.DEFAULT_HTTP_RETRY_HANDLER_INTERVAL_MAX,
                ConfigurationProperties.HTTP_RETRY_HANDLER_INTERVAL_MAX + "." + repository.getId(),
                ConfigurationProperties.HTTP_RETRY_HANDLER_INTERVAL_MAX);
        String serviceUnavailableCodesString = ConfigUtils.getString(
                session,
                ConfigurationProperties.DEFAULT_HTTP_RETRY_HANDLER_SERVICE_UNAVAILABLE,
                ConfigurationProperties.HTTP_RETRY_HANDLER_SERVICE_UNAVAILABLE + "." + repository.getId(),
                ConfigurationProperties.HTTP_RETRY_HANDLER_SERVICE_UNAVAILABLE);
        String retryHandlerName = ConfigUtils.getString(
                session,
                HTTP_RETRY_HANDLER_NAME_STANDARD,
                HTTP_RETRY_HANDLER_NAME + "." + repository.getId(),
                HTTP_RETRY_HANDLER_NAME);
        String userAgent = ConfigUtils.getString(
                session, ConfigurationProperties.DEFAULT_USER_AGENT, ConfigurationProperties.USER_AGENT);

        SocketConfig socketConfig =
                SocketConfig.custom().setSoTimeout(requestTimeout).build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectTimeout)
                .setLocalAddress(null)
                .setSocketTimeout(requestTimeout)
                .build();

        HttpRequestRetryHandler retryHandler;
        if (HTTP_RETRY_HANDLER_NAME_STANDARD.equals(retryHandlerName)) {
            retryHandler = new StandardHttpRequestRetryHandler(retryCount, false);
        } else if (HTTP_RETRY_HANDLER_NAME_DEFAULT.equals(retryHandlerName)) {
            retryHandler = new DefaultHttpRequestRetryHandler(retryCount, false);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported parameter " + HTTP_RETRY_HANDLER_NAME + " value: " + retryHandlerName);
        }
        Set<Integer> serviceUnavailableCodes = new HashSet<>();
        try {
            for (String code : ConfigUtils.parseCommaSeparatedUniqueNames(serviceUnavailableCodesString)) {
                serviceUnavailableCodes.add(Integer.parseInt(code));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Illegal HTTP codes for " + ConfigurationProperties.HTTP_RETRY_HANDLER_SERVICE_UNAVAILABLE
                            + " (list of integers): " + serviceUnavailableCodesString);
        }
        ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = new ResolverServiceUnavailableRetryStrategy(
                retryCount, retryInterval, retryIntervalMax, serviceUnavailableCodes);

        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent(userAgent)
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy)
                .setRetryHandler(retryHandler)
                .setConnectionManager(state.getConnectionManager())
                .setConnectionManagerShared(true)
                .setProxy(proxy);

        final boolean reuseConnections = ConfigUtils.getBoolean(
                session,
                ConfigurationProperties.DEFAULT_HTTP_REUSE_CONNECTIONS,
                ConfigurationProperties.HTTP_REUSE_CONNECTIONS + "." + repository.getId(),
                ConfigurationProperties.HTTP_REUSE_CONNECTIONS);
        if (!reuseConnections) {
            builder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        }

        this.client = builder.build();
    }

    private static HttpHost toHost(Proxy proxy) {
        HttpHost host = null;
        if (proxy != null) {
            host = new HttpHost(proxy.getHost(), proxy.getPort());
        }
        return host;
    }

    private URI resolve(TransportTask task) {
        return UriUtils.resolve(baseUri, task.getLocation());
    }

    @Override
    public int classify(Throwable error) {
        if (error instanceof HttpResponseException
                && ((HttpResponseException) error).getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return ERROR_NOT_FOUND;
        }
        return ERROR_OTHER;
    }

    @Override
    protected void implPeek(PeekTask task) throws Exception {
        HttpHead request = commonHeaders(new HttpHead(resolve(task)));
        execute(request, null);
    }

    @Override
    protected void implGet(GetTask task) throws Exception {
        boolean resume = true;

        EntityGetter getter = new EntityGetter(task);
        HttpGet request = commonHeaders(new HttpGet(resolve(task)));
        while (true) {
            try {
                if (resume) {
                    resume(request, task);
                }
                execute(request, getter);
                break;
            } catch (HttpResponseException e) {
                if (resume
                        && e.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED
                        && request.containsHeader(HttpHeaders.RANGE)) {
                    request = commonHeaders(new HttpGet(resolve(task)));
                    resume = false;
                    continue;
                }
                throw e;
            }
        }
    }

    private void execute(HttpUriRequest request, EntityGetter getter) throws Exception {
        try {
            SharingHttpContext context = new SharingHttpContext(state);
            try (CloseableHttpResponse response = client.execute(server, request, context)) {
                try {
                    context.close();
                    handleStatus(response);
                    if (getter != null) {
                        getter.handle(response);
                    }
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            }
        } catch (IOException e) {
            if (e.getCause() instanceof TransferCancelledException) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private boolean isPayloadPresent(HttpUriRequest request) {
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            return entity != null && entity.getContentLength() != 0;
        }
        return false;
    }

    private <T extends HttpUriRequest> T commonHeaders(T request) {
        request.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
        request.setHeader(HttpHeaders.PRAGMA, "no-cache");

        if (isPayloadPresent(request)) {
            request.setHeader(HttpHeaders.EXPECT, "100-continue");
        }

        for (Map.Entry<?, ?> entry : headers.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                continue;
            }
            if (entry.getValue() instanceof String) {
                request.setHeader(entry.getKey().toString(), entry.getValue().toString());
            } else {
                request.removeHeaders(entry.getKey().toString());
            }
        }

        return request;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private <T extends HttpUriRequest> T resume(T request, GetTask task) {
        long resumeOffset = task.getResumeOffset();
        if (resumeOffset > 0L && task.getDataFile() != null) {
            request.setHeader(HttpHeaders.RANGE, "bytes=" + resumeOffset + '-');
            request.setHeader(
                    HttpHeaders.IF_UNMODIFIED_SINCE,
                    DateUtils.formatDate(new Date(task.getDataFile().lastModified() - 60L * 1000L)));
            request.setHeader(HttpHeaders.ACCEPT_ENCODING, "identity");
        }
        return request;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void handleStatus(CloseableHttpResponse response) throws HttpResponseException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 300) {
            throw new HttpResponseException(status, response.getStatusLine().getReasonPhrase() + " (" + status + ")");
        }
    }

    @Override
    protected void implClose() {
        try {
            client.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        state.close();
    }

    private class EntityGetter {

        private final GetTask task;

        EntityGetter(GetTask task) {
            this.task = task;
        }

        public void handle(CloseableHttpResponse response) throws IOException, TransferCancelledException {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                entity = new ByteArrayEntity(new byte[0]);
            }

            long offset = 0L, length = entity.getContentLength();
            Header rangeHeader = response.getFirstHeader(HttpHeaders.CONTENT_RANGE);
            String range = rangeHeader != null ? rangeHeader.getValue() : null;
            if (range != null) {
                Matcher m = CONTENT_RANGE_PATTERN.matcher(range);
                if (!m.matches()) {
                    throw new IOException("Invalid Content-Range header for partial download: " + range);
                }
                offset = Long.parseLong(m.group(1));
                length = Long.parseLong(m.group(2)) + 1L;
                if (offset < 0L || offset >= length || (offset > 0L && offset != task.getResumeOffset())) {
                    throw new IOException("Invalid Content-Range header for partial download from offset "
                            + task.getResumeOffset() + ": " + range);
                }
            }

            final boolean resume = offset > 0L;
            final File dataFile = task.getDataFile();
            if (dataFile == null) {
                try (InputStream is = entity.getContent()) {
                    utilGet(task, is, true, length, resume);
                    extractChecksums(response);
                }
            } else {
                try (FileUtils.CollocatedTempFile tempFile = FileUtils.newTempFile(dataFile.toPath())) {
                    task.setDataFile(tempFile.getPath().toFile(), resume);
                    if (resume && Files.isRegularFile(dataFile.toPath())) {
                        try (InputStream inputStream = Files.newInputStream(dataFile.toPath())) {
                            Files.copy(inputStream, tempFile.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    try (InputStream is = entity.getContent()) {
                        utilGet(task, is, true, length, resume);
                    }
                    tempFile.move();
                } finally {
                    task.setDataFile(dataFile);
                }
            }
            if (task.getDataFile() != null) {
                Header lastModifiedHeader =
                        response.getFirstHeader(HttpHeaders.LAST_MODIFIED); // note: Wagon also does first not last
                if (lastModifiedHeader != null) {
                    Date lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());
                    if (lastModified != null) {
                        Files.setLastModifiedTime(
                                task.getDataFile().toPath(), FileTime.fromMillis(lastModified.getTime()));
                    }
                }
            }
            extractChecksums(response);
        }

        private void extractChecksums(CloseableHttpResponse response) {
            for (Map.Entry<String, ChecksumExtractor> extractorEntry : checksumExtractors.entrySet()) {
                Map<String, String> checksums = extractorEntry.getValue().extractChecksums(response);
                if (checksums != null) {
                    checksums.forEach(task::setChecksum);
                    return;
                }
            }
        }
    }

    private static class ResolverServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {
        private final int retryCount;

        private final long retryInterval;

        private final long retryIntervalMax;

        private final Set<Integer> serviceUnavailableHttpCodes;

        /**
         * Ugly, but forced by HttpClient API {@link ServiceUnavailableRetryStrategy}: the calls for
         * {@link #retryRequest(HttpResponse, int, HttpContext)} and {@link #getRetryInterval()} are done by same
         * thread and are actually done from spot that are very close to each other (almost subsequent calls).
         */
        private static final ThreadLocal<Long> RETRY_INTERVAL_HOLDER = new ThreadLocal<>();

        private ResolverServiceUnavailableRetryStrategy(
                int retryCount, long retryInterval, long retryIntervalMax, Set<Integer> serviceUnavailableHttpCodes) {
            if (retryCount < 0) {
                throw new IllegalArgumentException("retryCount must be >= 0");
            }
            if (retryInterval < 0L) {
                throw new IllegalArgumentException("retryInterval must be >= 0");
            }
            if (retryIntervalMax < 0L) {
                throw new IllegalArgumentException("retryIntervalMax must be >= 0");
            }
            this.retryCount = retryCount;
            this.retryInterval = retryInterval;
            this.retryIntervalMax = retryIntervalMax;
            this.serviceUnavailableHttpCodes = requireNonNull(serviceUnavailableHttpCodes);
        }

        @Override
        public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
            final boolean retry = executionCount <= retryCount
                    && (serviceUnavailableHttpCodes.contains(
                            response.getStatusLine().getStatusCode()));
            if (retry) {
                Long retryInterval = retryInterval(response, executionCount);
                if (retryInterval != null) {
                    RETRY_INTERVAL_HOLDER.set(retryInterval);
                    return true;
                }
            }
            RETRY_INTERVAL_HOLDER.remove();
            return false;
        }

        /**
         * Calculates retry interval in milliseconds. If {@link HttpHeaders#RETRY_AFTER} header present, it obeys it.
         * Otherwise, it returns {@link this#retryInterval} long value multiplied with {@code executionCount} (starts
         * from 1 and goes 2, 3,...).
         *
         * @return Long representing the retry interval as millis, or {@code null} if the request should be failed.
         */
        private Long retryInterval(HttpResponse httpResponse, int executionCount) {
            Long result = null;
            Header header = httpResponse.getFirstHeader(HttpHeaders.RETRY_AFTER);
            if (header != null && header.getValue() != null) {
                String headerValue = header.getValue();
                if (headerValue.contains(":")) { // is date when to retry
                    Date when = DateUtils.parseDate(headerValue); // presumably future
                    if (when != null) {
                        result = Math.max(when.getTime() - System.currentTimeMillis(), 0L);
                    }
                } else {
                    try {
                        result = Long.parseLong(headerValue) * 1000L; // is in seconds
                    } catch (NumberFormatException e) {
                        // fall through
                    }
                }
            }
            if (result == null) {
                result = executionCount * this.retryInterval;
            }
            if (result > retryIntervalMax) {
                return null;
            }
            return result;
        }

        @Override
        public long getRetryInterval() {
            Long ri = RETRY_INTERVAL_HOLDER.get();
            if (ri == null) {
                return 0L;
            }
            RETRY_INTERVAL_HOLDER.remove();
            return ri;
        }
    }
}
