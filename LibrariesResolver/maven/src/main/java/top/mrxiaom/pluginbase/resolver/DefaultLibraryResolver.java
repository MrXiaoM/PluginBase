package top.mrxiaom.pluginbase.resolver;

import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class DefaultLibraryResolver extends AbstractLibraryResolver {
    public DefaultLibraryResolver(Logger logger, File librariesDir, List<RemoteRepository> repositories) {
        super(logger, librariesDir, repositories);
    }
    public DefaultLibraryResolver(Logger logger, File librariesDir) {
        this(logger, librariesDir, getDefaultRepositories());
    }

    /**
     * 获取默认仓库列表
     * <ul>
     *     <li><a href="https://mirrors.huaweicloud.com/repository/maven">华为云镜像</a></li>
     *     <li><a href="https://repo.maven.apache.org/maven2">Maven Central 中心仓库</a></li>
     *     <li><a href="https://maven-central.storage-download.googleapis.com/maven2">Maven Central 中心仓库(海外)</a></li>
     * </ul>
     */
    public static List<RemoteRepository> getDefaultRepositories() {
        if (Locale.getDefault().getCountry().equals("CN")) {
            return Arrays.asList(
                    new RemoteRepository.Builder("huaweicloud", "default", "https://mirrors.huaweicloud.com/repository/maven").build(),
                    new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build()
            );
        } else {
            String central = System.getenv("PAPER_DEFAULT_CENTRAL_REPOSITORY");
            if (central == null) {
                central = System.getProperty("org.bukkit.plugin.java.LibraryLoader.centralURL");
            }
            if (central == null) {
                central = "https://maven-central.storage-download.googleapis.com/maven2";
            }
            return Collections.singletonList(new RemoteRepository.Builder("central", "default", central).build());
        }
    }
}
