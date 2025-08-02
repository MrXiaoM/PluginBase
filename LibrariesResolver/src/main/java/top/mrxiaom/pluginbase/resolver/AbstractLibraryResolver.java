package top.mrxiaom.pluginbase.resolver;

import top.mrxiaom.pluginbase.resolver.aether.DefaultRepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.RepositorySystem;
import top.mrxiaom.pluginbase.resolver.aether.artifact.Artifact;
import top.mrxiaom.pluginbase.resolver.aether.artifact.DefaultArtifact;
import top.mrxiaom.pluginbase.resolver.aether.collection.CollectRequest;
import top.mrxiaom.pluginbase.resolver.maven.connector.BasicRepositoryConnectorFactory;
import top.mrxiaom.pluginbase.resolver.aether.graph.Dependency;
import top.mrxiaom.pluginbase.resolver.aether.impl.DefaultServiceLocator;
import top.mrxiaom.pluginbase.resolver.aether.repository.LocalRepository;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.repository.RepositoryPolicy;
import top.mrxiaom.pluginbase.resolver.aether.resolution.ArtifactResult;
import top.mrxiaom.pluginbase.resolver.aether.resolution.DependencyRequest;
import top.mrxiaom.pluginbase.resolver.aether.resolution.DependencyResolutionException;
import top.mrxiaom.pluginbase.resolver.aether.resolution.DependencyResult;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.RepositoryConnectorFactory;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.TransporterFactory;
import top.mrxiaom.pluginbase.resolver.aether.transfer.AbstractTransferListener;
import top.mrxiaom.pluginbase.resolver.aether.transfer.TransferEvent;
import top.mrxiaom.pluginbase.resolver.maven.provider.MavenRepositorySystemUtils;
import top.mrxiaom.pluginbase.resolver.maven.transport.HttpTransporterFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base on <code>org.bukkit.plugin.java.LibraryLoader</code> from Purpur 1.20.4
 */
public abstract class AbstractLibraryResolver {
    protected final Logger logger;
    protected final RepositorySystem repository;
    protected final DefaultRepositorySystemSession session;
    protected List<RemoteRepository> repositories;
    protected final List<String> libraries = new ArrayList<>();

    public AbstractLibraryResolver(Logger logger, File librariesDir, List<RemoteRepository> repositories) {
        this.logger = logger;

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        this.repository = locator.getService(RepositorySystem.class);
        if (this.repository == null) {
            throw new IllegalStateException("Can't get RepositorySystem service!");
        }
        this.session = MavenRepositorySystemUtils.newSession();

        session.setSystemProperties(System.getProperties());
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        session.setLocalRepositoryManager(repository.newLocalRepositoryManager(session, new LocalRepository(librariesDir)));
        session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferStarted(@NotNull TransferEvent event) {
                onTransferStarted(event);
            }
        });
        onCreateSessionPost(session);
        session.setReadOnly();

        this.repositories = repository.newResolutionRepositories(session, repositories);
    }

    protected void onCreateSessionPost(DefaultRepositorySystemSession session) {
    }

    protected void onTransferStarted(TransferEvent event) {
        String resourceName = event.getResource().getResourceName();
        if (resourceName.endsWith(".pom")) {
            logger.log(Level.INFO, "正在获取依赖关系 {0}", event.getResource().getRepositoryUrl() + resourceName);
        } else {
            logger.log(Level.INFO, "正在下载依赖 {0}", event.getResource().getRepositoryUrl() + resourceName);
        }
    }

    public void setRepositories(List<RemoteRepository> repositories) {
        this.repositories = repository.newResolutionRepositories(session, repositories);
    }

    /**
     * 获取依赖列表，使用<code>组名:制品名:版本</code>格式
     */
    public List<String> getLibraries() {
        return libraries;
    }

    /**
     * 设置依赖列表，使用<code>组名:制品名:版本</code>格式
     */
    public void setLibraries(List<String> libraries) {
        this.libraries.clear();
        if (!libraries.isEmpty()) {
            this.libraries.addAll(libraries);
        }
    }

    /**
     * 添加依赖，使用<code>组名:制品名:版本</code>格式
     */
    public void addLibrary(String... libraries) {
        this.libraries.addAll(Arrays.asList(libraries));
    }

    /**
     * 添加依赖，使用<code>组名:制品名:版本</code>格式
     */
    public void addLibraries(Iterable<String> libraries) {
        for (String library : libraries) {
            this.libraries.add(library);
        }
    }

    /**
     * 检查并下载依赖
     * @return 依赖文件列表
     */
    public List<URL> doResolve() {
        if (libraries.isEmpty()) return Collections.emptyList();

        List<Dependency> dependencies = new ArrayList<>();
        for (String library : libraries) {
            Artifact artifact = new DefaultArtifact(library);
            dependencies.add(new Dependency(artifact, null));
        }

        DependencyResult result;
        try {
            CollectRequest collectRequest = new CollectRequest((Dependency) null, dependencies, repositories);
            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
            result = repository.resolveDependencies(session, dependencyRequest);
        } catch ( DependencyResolutionException ex ) {
            throw new RuntimeException("处理依赖时发生错误", ex);
        }

        List<URL> jarFiles = new ArrayList<>();
        for (ArtifactResult artifact : result.getArtifactResults()) {
            File file = artifact.getArtifact().getFile();
            try {
                jarFiles.add(file.toURI().toURL());
            } catch (MalformedURLException ex) {
                throw new AssertionError(ex);
            }
        }
        return jarFiles;
    }
}
