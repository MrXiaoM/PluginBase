package top.mrxiaom.pluginbase.func.language;

import com.google.common.collect.Lists;
import top.mrxiaom.pluginbase.func.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public class LanguageEnumAutoHolder<T extends Enum<T>> extends AbstractLanguageHolder {
    private static <T extends Enum<T>> String key(Enum<T> enumValue) {
        String name = enumValue.name().replace("__", ".").replace("_", "-");
        Language lang = enumValue.getClass().getAnnotation(Language.class);
        return lang == null ? name : (lang.prefix() + name);
    }
    public final Enum<T> enumValue;
    public LanguageEnumAutoHolder(Enum<T> enumValue, boolean isList, Object defaultValue) {
        super(key(enumValue), isList, defaultValue);
        this.enumValue = enumValue;
    }

    @Override
    public LanguageManager getLanguageManager() {
        return LanguageManager.inst();
    }

    public static <T extends Enum<T>> LanguageEnumAutoHolder<T> wrap(Enum<T> e, String defaultValue) {
        return new LanguageEnumAutoHolder<>(e, false, defaultValue);
    }
    public static <T extends Enum<T>> LanguageEnumAutoHolder<T> wrap(Enum<T> e, List<String> defaultValue) {
        return new LanguageEnumAutoHolder<>(e, true, defaultValue);
    }
    public static <T extends Enum<T>> LanguageEnumAutoHolder<T> wrap(Enum<T> e, String... defaultValue) {
        List<String> def = Lists.newArrayList(defaultValue);
        return new LanguageEnumAutoHolder<>(e, true, def);
    }
}
