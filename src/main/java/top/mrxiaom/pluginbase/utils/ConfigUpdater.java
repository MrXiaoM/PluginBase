package top.mrxiaom.pluginbase.utils;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.AbstractPluginHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfigUpdater extends AbstractPluginHolder<BukkitPlugin> {
    public static final boolean supportComments = checkSupportComments();
    private static boolean checkSupportComments() {
        try {
            MemorySection.class.getDeclaredMethod("getComments", String.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
    public static final Function<String, Boolean> ALL_MATCHER = (s) -> true;
    private final YamlConfiguration defaultConfig;
    private final List<String> autoSaveFullMatch = new ArrayList<>();
    private final List<String> autoSavePrefixMatch = new ArrayList<>();
    private final List<Function<String, Boolean>> autoSaveCustomMatch = new ArrayList<>();
    private ConfigUpdater(BukkitPlugin plugin, YamlConfiguration defaultConfig) {
        super(plugin);
        this.defaultConfig = defaultConfig;
    }

    private static boolean notEquals(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) return true;
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加要保存的配置键（完全匹配）
     */
    public ConfigUpdater fullMatch(String s) {
        this.autoSaveFullMatch.add(s);
        return this;
    }

    /**
     * 添加要保存的配置键（前缀匹配）
     */
    public ConfigUpdater prefixMatch(String s) {
        this.autoSavePrefixMatch.add(s);
        return this;
    }

    /**
     * 添加要保存的配置键（自定义匹配）
     */
    public ConfigUpdater customMatch(Function<String, Boolean> func) {
        this.autoSaveCustomMatch.add(func);
        return this;
    }

    public boolean isKeyMatch(String key) {
        for (String s : autoSaveFullMatch) {
            if (key.equals(s)) return true;
        }
        for (String s : autoSavePrefixMatch) {
            if (key.startsWith(s)) return true;
        }
        for (Function<String, Boolean> func : autoSaveCustomMatch) {
            if (func.apply(key)) return true;
        }
        return false;
    }

    /**
     * 更新配置
     * @param config 目标配置
     * @param saveFile 保存到文件
     */
    public ConfigUpdater apply(@NotNull YamlConfiguration config, @Nullable File saveFile) {
        boolean modified = false;

        // 遍历默认配置的所有键
        for (String key : defaultConfig.getKeys(true)) {
            // !contains(key, ignoreDefault=false)
            if (defaultConfig.get(key, null) == null) {
                continue;
            }
            // 如果目标配置里没有
            if (config.get(key, null) == null) {
                if (isKeyMatch(key)) { // 将应该更新的配置添加进去
                    config.set(key, defaultConfig.get(key));
                    if (supportComments) {
                        config.setComments(key, defaultConfig.getComments(key));
                        config.setInlineComments(key, defaultConfig.getInlineComments(key));
                    }
                    modified = true;
                }
            } else if (supportComments) { // 反之，检查注释是否一致
                List<String> comments = config.getComments(key);
                List<String> defComments = defaultConfig.getComments(key);
                if (notEquals(comments, defComments)) {
                    config.setComments(key, defComments);
                    modified = true;
                }
                List<String> inlineComments = config.getInlineComments(key);
                List<String> defInlineComments = defaultConfig.getInlineComments(key);
                if (notEquals(inlineComments, defInlineComments)) {
                    config.setInlineComments(key, defInlineComments);
                    modified = true;
                }
            }
        }

        if (modified && saveFile != null) {
            try {
                config.save(saveFile);
            } catch (IOException e) {
                warn(e);
            }
        }
        return this;
    }

    @NotNull
    public static ConfigUpdater create(BukkitPlugin plugin, String resourcePath) {
        return create(plugin, resourcePath, '.');
    }

    @NotNull
    public static ConfigUpdater create(BukkitPlugin plugin, String resourcePath, char pathSeparator) {
        YamlConfiguration defaultConfig = new YamlConfiguration();
        defaultConfig.options().pathSeparator(pathSeparator);
        InputStream resource = plugin.getResource(resourcePath);
        if (resource != null) try (InputStream input = resource;
             InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            defaultConfig.load(reader);
        } catch (Exception e) {
            plugin.warn("无法读取默认配置 " + resourcePath, e);
        } else {
            plugin.warn("默认配置不存在 " + resourcePath);
        }
        return create(plugin, defaultConfig);
    }

    @NotNull
    public static ConfigUpdater create(BukkitPlugin plugin, YamlConfiguration defaultConfig) {
        return new ConfigUpdater(plugin, defaultConfig);
    }
}
