package top.mrxiaom.pluginbase.resolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.resolver.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.utils.Sha1Checksum;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractLibraryResolver {
    protected final Logger logger;
    protected final File librariesDir;
    protected List<RemoteRepository> repositories;
    protected final List<String> libraries = new ArrayList<>();
    protected Map<String, String> startsReplacer = new HashMap<>();
    protected List<URL> lastResolve = null;
    protected String userAgent;

    public AbstractLibraryResolver(Logger logger, File librariesDir, List<RemoteRepository> repositories) {
        this.logger = logger;
        this.librariesDir = librariesDir;
        this.repositories = repositories;
        this.userAgent = createUserAgent("LibrariesResolver", BuildConstants.VERSION);
    }

    protected void onTransferStarted(RemoteRepository repository, String path) {
        logger.log(Level.INFO, "正在下载依赖 {0}", repository.getUrl() + path);
    }

    public void setRepositories(@NotNull List<RemoteRepository> repositories) {
        this.repositories = repositories;
    }

    @NotNull
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(@NotNull String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 获取依赖列表，使用<code>文件路径</code>格式
     */
    @NotNull
    public List<String> getResolvedLibraries() {
        return libraries;
    }

    /**
     * 设置依赖列表，使用<code>文件路径</code>格式
     */
    public void setResolvedLibraries(@NotNull List<String> libraries) {
        this.libraries.clear();
        if (!libraries.isEmpty()) {
            this.libraries.addAll(libraries);
        }
    }

    /**
     * 添加依赖，使用<code>文件路径</code>格式
     */
    public void addResolvedLibrary(String @NotNull... libraries) {
        this.libraries.addAll(Arrays.asList(libraries));
    }

    /**
     * 添加依赖，使用<code>文件路径</code>格式
     */
    public void addResolvedLibraries(@NotNull Iterable<String> libraries) {
        for (String library : libraries) {
            this.libraries.add(library);
        }
    }

    public Map<String, String> getStartsReplacer() {
        return startsReplacer;
    }

    public void setStartsReplacer(Map<String, String> startsReplacer) {
        this.startsReplacer = startsReplacer;
    }

    protected void onDownloadFailed(String uri, Exception ex) {
        logger.log(Level.WARNING, "无法下载文件 " + uri, ex);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean downloadFile(String uri, File destination) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", userAgent);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode);
            }

            // 创建文件夹
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            // 下载并保存文件
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 FileOutputStream out = new FileOutputStream(destination)
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            } finally {
                conn.disconnect();
            }
            return true;
        } catch (Exception e) {
            onDownloadFailed(uri, e);
        }
        return false;
    }

    private boolean download(String path, File destination) {
        for (RemoteRepository repository : repositories) {
            String s = repository.getUrl() + path;
            onTransferStarted(repository, path);
            // 下载 jar 文件
            if (downloadFile(s, destination)) {
                // 如果成功，下载 .sha1 校验文件
                File sha1File = Sha1Checksum.getChecksumFile(destination);
                if (!downloadFile(s + ".sha1", sha1File)) continue;
                if (!Sha1Checksum.checksum(destination)) continue;
                // 如果下载成功并校验成功，返回 true
                return true;
            }
        }
        return false;
    }

    private String map(String oldLink) {
        for (Map.Entry<String, String> entry : startsReplacer.entrySet()) {
            if (oldLink.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return oldLink;
    }

    /**
     * 检查并下载依赖
     * @return 依赖文件列表
     */
    @NotNull
    public List<URL> doResolve() {
        if (libraries.isEmpty()) return Collections.emptyList();

        List<URL> jarFiles = new ArrayList<>();
        for (String oldLink : libraries) {
            String library = map(oldLink);
            File file = new File(librariesDir, library);
            // 如果校验失败，下载文件
            if (!Sha1Checksum.checksum(file)) {
                // 如果下载文件失败，抛出异常
                if (!download(library, file)) {
                    throw new RuntimeException("处理依赖 " + library + " 失败，详见上方日志");
                }
            }
            // 如果校验成功，或者下载成功，添加结果
            try {
                jarFiles.add(file.toURI().toURL());
            } catch (MalformedURLException ex) {
                throw new AssertionError(ex);
            }
        }
        return lastResolve = Collections.unmodifiableList(jarFiles);
    }

    @Nullable
    public List<URL> getLastResolve() {
        return lastResolve;
    }

    public static String createUserAgent(String toolName, String toolVersion) {
        Properties props = System.getProperties();
        String javaVersion = props.getProperty("java.version", "unknown");
        String osName = props.getProperty("os.name", "unknown");
        String osArch = props.getProperty("os.arch", "unknown");
        return String.format("%s/%s (Java %s; %s; %s)", toolName, toolVersion, javaVersion, osName, osArch);
    }
}
