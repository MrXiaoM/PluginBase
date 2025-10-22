package top.mrxiaom.pluginbase.resolver.repository;

import org.jetbrains.annotations.NotNull;

public class RemoteRepository {
    private final String name;
    private final String url;

    public RemoteRepository(@NotNull String name, @NotNull String url) {
        this.name = name;
        this.url = url.endsWith("/") ? url : (url + "/");
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getUrl() {
        return url;
    }
}
