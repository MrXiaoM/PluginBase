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
            Language langParent = parent.getAnnotation(Language.class);
            Language langField = field.getAnnotation(Language.class);
            String name;
            if (langField != null && !langField.value().isEmpty()) {
                name = langField.value();
            } else {
                name = field.getName().replace("__", ".").replace("_", "-");
            }
            key(langParent == null ? name : (langParent.prefix() + name));
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
