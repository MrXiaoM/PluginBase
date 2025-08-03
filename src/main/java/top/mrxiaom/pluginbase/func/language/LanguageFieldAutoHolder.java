package top.mrxiaom.pluginbase.func.language;

import com.google.common.collect.Lists;
import top.mrxiaom.pluginbase.func.LanguageManager;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 为 public static final 字段储存语言元数据
 */
public class LanguageFieldAutoHolder extends Message {
    private LanguageManager manager;
    private LanguageFieldAutoHolder(List<String> defaultValue) {
        super("", defaultValue);
    }
    private LanguageFieldAutoHolder(String defaultValue) {
        super("", defaultValue);
    }
    public void lateInitFromField(Class<?> parent, Field field) {
        if (key().isEmpty()) {
            String name = field.getName().replace("__", ".").replace("_", "-");
            Language lang = parent.getAnnotation(Language.class);
            key(lang == null ? name : (lang.prefix() + name));
        }
    }

    @Override
    public LanguageManager getLanguageManager() {
        if (manager != null) {
            return manager;
        }
        return manager = LanguageManager.inst();
    }

    public static LanguageFieldAutoHolder field(String defaultValue) {
        return new LanguageFieldAutoHolder(defaultValue);
    }
    public static LanguageFieldAutoHolder field(List<String> defaultValue) {
        return new LanguageFieldAutoHolder(defaultValue);
    }
    public static LanguageFieldAutoHolder field(String... defaultValue) {
        List<String> def = Lists.newArrayList(defaultValue);
        return new LanguageFieldAutoHolder(def);
    }
}
