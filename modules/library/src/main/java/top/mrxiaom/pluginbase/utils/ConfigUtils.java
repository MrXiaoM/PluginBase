package top.mrxiaom.pluginbase.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigUtils {
    /**
     * 加载配置文件
     * @see ConfigUtils#load(FileConfiguration, File, Charset)
     */
    public static YamlConfiguration load(File file) {
        return load(new YamlConfiguration(), file);
    }

    /**
     * 加载配置文件
     * @see ConfigUtils#load(FileConfiguration, File, Charset)
     */
    public static YamlConfiguration load(File file, Charset charset) {
        return load(new YamlConfiguration(), file, charset);
    }

    /**
     * 加载配置文件
     * @see ConfigUtils#load(FileConfiguration, File, Charset)
     */
    public static <T extends FileConfiguration> T load(T config, File file) {
        return load(config, file, StandardCharsets.UTF_8);
    }

    /**
     * 加载配置文件
     * @param config 目标配置
     * @param file 文件
     * @param charset 编码
     */
    public static <T extends FileConfiguration> T load(T config, File file, Charset charset) {
        try (FileInputStream stream = new FileInputStream(file)) {
            config.load(new InputStreamReader(stream, charset));
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
        return config;
    }

    /**
     * 保存配置文件
     * @see ConfigUtils#save(FileConfiguration, File, Charset)
     */
    public static void save(FileConfiguration config, File file) throws IOException {
        save(config, file, StandardCharsets.UTF_8);
    }

    /**
     * 保存配置文件
     * @param config 配置文件
     * @param file 文件
     * @param charset 编码
     */
    public static void save(FileConfiguration config, File file, Charset charset) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            Util.mkdirs(parent);
        }
        String data = config.saveToString();

        try (FileOutputStream output = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(output, charset)) {
            writer.write(data);
        }
    }

    /**
     * <code>getMapList</code> 的升级版，更方便地操作 Array 套 Object 的配置格式。<br>
     * 你可以直接将 <code>List&lt;ConfigurationSection&gt;</code> 类型的值传入 <code>set(String, Object)</code>，如下所示
     * <pre><code>
     * List&lt;ConfigurationSection&gt; list = new ArrayList&lt;&gt;();
     * list.add(new MemoryConfiguration());
     * config.set("foo", list);
     * </code></pre>
     * @param config 配置
     * @param key 键
     * @return 一个新的列表，对其进行修改不会对原配置进行修改，需要执行 <code>config.set(String, Object)</code> 应用修改
     * @see ConfigurationSection#getMapList(String)
     */
    @NotNull
    public static List<ConfigurationSection> getSectionList(ConfigurationSection config, String key) {
        List<ConfigurationSection> list = new ArrayList<>();
        List<?> rawList = config.getList(key, null);
        if (rawList == null) return list;
        for (Object obj : rawList) {
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                MemoryConfiguration section = new MemoryConfiguration();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String sectionKey = entry.getKey().toString();
                    section.set(sectionKey, processValue(section, sectionKey, entry.getValue()));
                }
                list.add(section);
                continue;
            }
            if (obj instanceof ConfigurationSection) {
                list.add((ConfigurationSection) obj);
            }
        }
        return list;
    }

    private static Object processValue(ConfigurationSection parent, String key, Object value) {
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            ConfigurationSection section;
            if (parent == null || key == null) { // 兼容 List
                section = new MemoryConfiguration();
            } else { // 兼容 Map
                section = parent.createSection(key);
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String mapKey = entry.getKey().toString();
                section.set(mapKey, processValue(section, mapKey, entry.getValue()));
            }
            return section;
        }
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            List<Object> result = new ArrayList<>();
            for (Object object : list) {
                result.add(processValue(null, null, object));
            }
            return result;
        }
        return value;
    }

    /**
     * 读取普通小数 (<code>0.5</code>) 或百分数 (<code>50%</code>) 为 <code>1.0 = 100%</code> 的浮点数形式
     * @param section 配置
     * @param key 键
     * @param def 默认值
     */
    @Contract("_,_,!null->!null")
    public static Double getPercentAsDouble(@NotNull ConfigurationSection section, @NotNull String key, @Nullable Double def) {
        return getPercentAsDouble(section.getString(key, null), def);
    }

    /**
     * 读取普通小数 (<code>0.5</code>) 或百分数 (<code>50%</code>) 为 <code>1.0 = 100%</code> 的浮点数形式
     * @param section 配置
     * @param key 键
     * @param def 默认值
     */
    @Contract("_,_,!null->!null")
    public static Float getPercentAsFloat(@NotNull ConfigurationSection section, @NotNull String key, @Nullable Float def) {
        return getPercentAsFloat(section.getString(key, null), def);
    }

    /**
     * 读取普通小数 (<code>0.5</code>) 或百分数 (<code>50%</code>) 为 <code>1.0 = 100%</code> 的浮点数形式
     * @param s 要转换的字符串
     * @param def 默认值
     */
    @Contract("_,!null->!null")
    public static Double getPercentAsDouble(@Nullable String s, @Nullable Double def) {
        if (s == null) return def;
        try {
            if (s.endsWith("%")) {
                String str = s.substring(0, s.length() - 1);
                double value = Double.parseDouble(str);
                return value / 100.0;
            } else {
                return Double.parseDouble(s);
            }
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 读取普通小数 (<code>0.5</code>) 或百分数 (<code>50%</code>) 为 <code>1.0 = 100%</code> 的浮点数形式
     * @param s 要转换的字符串
     * @param def 默认值
     */
    @Contract("_,!null->!null")
    public static Float getPercentAsFloat(String s, Float def) {
        if (s == null) return def;
        try {
            if (s.endsWith("%")) {
                String str = s.substring(0, s.length() - 1);
                float value = Float.parseFloat(str);
                return value / 100.0f;
            } else {
                return Float.parseFloat(s);
            }
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 获取可空的 Double 类型配置数据
     * @param section 配置
     * @param key 键
     * @param def 默认值
     */
    public static Double getDouble(ConfigurationSection section, String key, Double def) {
        return section.contains(key) && section.isDouble(key) ? Double.valueOf(section.getDouble(key)) : def;
    }

    /**
     * 获取可空的 Integer 类型配置数据
     * @param section 配置
     * @param key 键
     * @param def 默认值
     */
    public static Integer getInt(ConfigurationSection section, String key, Integer def) {
        return section.contains(key) && section.isInt(key) ? Integer.valueOf(section.getInt(key)) : def;
    }

    public static Material getItem(ConfigurationSection section, String key, Material def) {
        if (section == null) return def;
        return Util.valueOr(Material.class, section.getString(key), def);
    }
}
