package top.mrxiaom.pluginbase.func;

import com.google.common.collect.Lists;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.language.*;
import top.mrxiaom.pluginbase.utils.ConfigUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class LanguageManager extends AbstractPluginHolder<BukkitPlugin> {
    @SuppressWarnings({"rawtypes"})
    private final Map<String, Function> holderGetters = new HashMap<>();
    private final Map<String, Object> holderValues = new HashMap<>();
    private final Map<String, AbstractLanguageHolder> holders = new HashMap<>();
    private ILanguageArgumentProcessor processor = (holder, key, value) -> value;
    private File file = null;
    /**
     * 是否禁止在重载配置文件时重载语言文件
     */
    private boolean disableReloadConfig = false;
    /**
     * 语言键前缀
     */
    private String keyPrefix = "";
    public LanguageManager(BukkitPlugin plugin) {
        super(plugin);
    }

    /**
     * 获取语言文件路径
     */
    @Nullable
    public File getLangFile() {
        return file;
    }

    /**
     * 设置语言文件路径
     */
    public LanguageManager setLangFile(@Nullable String langFile) {
        if (langFile == null) {
            return setLangFile((File) null);
        } else {
            return setLangFile(new File(plugin.getDataFolder(), langFile));
        }
    }

    /**
     * 设置语言文件路径
     */
    public LanguageManager setLangFile(@Nullable File file) {
        this.file = file;
        return this;
    }

    public ILanguageArgumentProcessor getProcessor() {
        return processor;
    }

    /**
     * 设置变量参数处理器
     */
    public LanguageManager setProcessor(ILanguageArgumentProcessor processor) {
        this.processor = processor;
        return this;
    }

    public boolean isDisableReloadConfig() {
        return disableReloadConfig;
    }

    public LanguageManager setDisableReloadConfig(boolean disableReloadConfig) {
        this.disableReloadConfig = disableReloadConfig;
        return this;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public LanguageManager setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
        return this;
    }

    /**
     * 注册枚举到语言管理器，示例如下
     * <pre><code>
     * public enum Messages implements IHolderAccessor {
     *     path__to__key("默认值")
     *     ;
     *     Messages(String defaultValue) {
     *         holder = wrap(this, defaultValue);
     *     }
     *     Messages(String... defaultValue) {
     *         holder = wrap(this, defaultValue);
     *     }
     *     Messages(List<String> defaultValue) {
     *         holder = wrap(this, defaultValue);
     *     }
     *     private final LanguageEnumAutoHolder<Messages> holder;
     *     public LanguageEnumAutoHolder<Messages> holder() {
     *         return holder;
     *     }
     * }
     * </code></pre>
     * 在枚举名中，<code>__</code> 会被替换为 <code>.</code>，<code>_</code> 会被替换为 <code>-</code> 作为键名。<br>
     * @param enumType 枚举类型
     * @param getter 获取 holder 实例的 getter，如 <code>Messages::holder</code>
     * @see LanguageEnumAutoHolder#wrap(Enum, String)
     * @see IHolderAccessor
     */
    public <T extends Enum<T>> LanguageManager register(Class<T> enumType, Function<T, LanguageEnumAutoHolder<T>> getter) {
        holderGetters.put(enumType.getName(), getter);
        for (T value : enumType.getEnumConstants()) {
            LanguageEnumAutoHolder<T> holder = getter.apply(value);
            holders.put(holder.key(), holder);
        }
        return this;
    }

    /**
     * 通过读取 public static final 字段，注册到语言管理器。<br>
     * 要求字段引用的实现必须为 <code>LanguageFieldAutoHolder</code> 或其子类，如
     * <pre><code>
     * public static final Message path__to__key = field("默认值");
     * </code></pre>
     * 在字段名中，<code>__</code> 会被替换为 <code>.</code>，<code>_</code> 会被替换为 <code>-</code> 作为键名。<br>
     * 你也可以添加 <code>@Language("path.to.key")</code> 来自定义键名。
     * @param anyType 任意类型
     * @see LanguageFieldAutoHolder#field(String)
     * @see Message
     */
    public LanguageManager register(Class<?> anyType) {
        if (anyType.isAssignableFrom(Enum.class)) {
            throw new IllegalArgumentException("应该使用 register(Class, Function) 来注册 enum");
        }
        for (Field field : anyType.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                LanguageFieldAutoHolder holder = getOrNull(field, null);
                if (holder != null) {
                    holder.lateInitFromField(anyType, field);
                    holders.put(holder.key(), holder);
                }
            }
        }
        return this;
    }

    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Enum<T>> LanguageEnumAutoHolder<T> getHolderByEnum(T value) {
        Function getter = holderGetters.get(value.getClass().getName());
        if (getter == null) return null;
        return (LanguageEnumAutoHolder<T>) getter.apply(value);
    }

    public List<AbstractLanguageHolder> getHolders() {
        return Lists.newArrayList(holders.values());
    }

    /**
     * 用于 AbstractLanguageHolder，一般不直接调用
     */
    @Nullable
    public String getAsString(String key) {
        Object obj = holderValues.get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    /**
     * 用于 AbstractLanguageHolder，一般不直接调用
     */
    @Nullable
    @SuppressWarnings({"unchecked"})
    public List<String> getAsList(String key) {
        Object obj = holderValues.get(key);
        if (obj instanceof List<?>) {
            return (List<String>) obj;
        }
        return null;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        if (disableReloadConfig) return;
        reload();
    }

    /**
     * 重载语言文件
     */
    public LanguageManager reload() {
        if (file == null || holders.isEmpty()) return this;
        holderValues.clear();
        YamlConfiguration config = ConfigUtils.load(file);
        config.setDefaults(new YamlConfiguration());
        for (AbstractLanguageHolder holder : holders.values()) {
            if (!config.contains(holder.key())) {
                config.set(keyPrefix + holder.key(), holder.defaultValue);
                continue;
            }
            if (holder.isList) {
                holderValues.put(keyPrefix + holder.key(), config.getStringList(holder.key()));
            } else {
                holderValues.put(keyPrefix + holder.key(), config.getString(holder.key()));
            }
        }
        try {
            ConfigUtils.save(config, file);
        } catch (IOException e) {
            warn("更新语言文件时出现异常", e);
        }
        return this;
    }

    /**
     * 获取 PluginBase 的默认 LanguageManager 实例
     */
    public static LanguageManager inst() {
        return instanceOf(LanguageManagerImpl.class);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <T> T getOrNull(Field field, Object instance) {
        try {
            Object object = field.get(instance);
            return (T) object;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
