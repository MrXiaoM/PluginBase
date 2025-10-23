package top.mrxiaom.pluginbase.func.language;

import com.google.common.collect.Lists;
import top.mrxiaom.pluginbase.func.LanguageManager;

import java.util.List;

/**
 * 为 enum 储存语言元数据
 */
public class LanguageEnumAutoHolder<T extends Enum<T>> extends Message {
    private static <T extends Enum<T>> String key(Enum<T> enumValue) {
        String name = enumValue.name().replace("__", ".").replace("_", "-");
        Language lang = enumValue.getClass().getAnnotation(Language.class);
        return lang == null ? name : (lang.prefix() + name);
    }
    public final Enum<T> enumValue;
    private LanguageManager manager;
    private LanguageEnumAutoHolder(Enum<T> enumValue, List<String> defaultValue) {
        super(key(enumValue), defaultValue);
        this.enumValue = enumValue;
    }
    private LanguageEnumAutoHolder(Enum<T> enumValue, String defaultValue) {
        super(key(enumValue), defaultValue);
        this.enumValue = enumValue;
    }

    @Override
    public LanguageManager getLanguageManager() {
        if (manager != null) {
            return manager;
        }
        return manager = LanguageManager.inst();
    }

    public static <T extends Enum<T>> LanguageEnumAutoHolder<T> wrap(Enum<T> e, String defaultValue) {
        return new LanguageEnumAutoHolder<>(e, defaultValue);
    }
    public static <T extends Enum<T>> LanguageEnumAutoHolder<T> wrap(Enum<T> e, List<String> defaultValue) {
        return new LanguageEnumAutoHolder<>(e, defaultValue);
    }
    public static <T extends Enum<T>> LanguageEnumAutoHolder<T> wrap(Enum<T> e, String... defaultValue) {
        List<String> def = Lists.newArrayList(defaultValue);
        return new LanguageEnumAutoHolder<>(e, def);
    }
}
