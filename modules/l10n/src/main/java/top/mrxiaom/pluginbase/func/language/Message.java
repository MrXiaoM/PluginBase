package top.mrxiaom.pluginbase.func.language;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 作为 AbstractLanguageHolder 的别名
 */
public abstract class Message extends AbstractLanguageHolder {
    public Message(@NotNull String key, List<String> defaultValue) {
        super(key, defaultValue);
    }
    public Message(@NotNull String key, String defaultValue) {
        super(key, defaultValue);
    }
}
