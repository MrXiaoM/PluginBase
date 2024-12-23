package top.mrxiaom.pluginbase.func.language;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static top.mrxiaom.pluginbase.utils.Pair.replace;

public abstract class AbstractLanguageHolder {
    public final String key;
    public final boolean isList;
    public final Object defaultValue;

    public AbstractLanguageHolder(@NotNull String key, List<String> defaultValue) {
        this.key = key;
        this.isList = true;
        this.defaultValue = defaultValue;
    }
    public AbstractLanguageHolder(@NotNull String key, String defaultValue) {
        this.key = key;
        this.isList = false;
        this.defaultValue = defaultValue;
    }

    public abstract LanguageManager getLanguageManager();

    @SuppressWarnings({"unchecked"})
    private <T> T getOrDefault(T value) {
        return value == null ? (T) defaultValue : value;
    }

    public String str() {
        LanguageManager lang = getLanguageManager();
        if (isList) {
            List<String> list = getOrDefault(lang.getAsList(key));
            return String.join("\n", list);
        } else {
            return getOrDefault(lang.getAsString(key));
        }
    }
    public String str(Object... args) {
        return String.format(str(), args);
    }
    @SafeVarargs
    public final String str(Pair<String, Object>... replacements) {
        return replace(str(), replacements);
    }
    public List<String> list() {
        LanguageManager lang = getLanguageManager();
        if (isList) {
            return getOrDefault(lang.getAsList(key));
        } else {
            String str = getOrDefault(lang.getAsString(key));
            return Lists.newArrayList(str.split("\n"));
        }
    }
    public List<String> list(Object... args) {
        return list().stream()
                .map(it -> String.format(it, args))
                .collect(Collectors.toList());
    }
    @SafeVarargs
    public final List<String> list(Pair<String, Object>... replacements) {
        return list().stream()
                .map(it -> replace(str(), replacements))
                .collect(Collectors.toList());
    }

    /**
     * 发送消息
     * @param receiver 消息接收者
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean t(CommandSender receiver) {
        ColorHelper.parseAndSend(receiver, str());
        return true;
    }
    /**
     * 发送消息
     * @param receiver 消息接收者
     * @param args <code>String.format</code> 参数
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean t(CommandSender receiver, Object... args) {
        ColorHelper.parseAndSend(receiver, str(args));
        return true;
    }
    /**
     * 发送消息
     * @param receiver 消息接收者
     * @param replacements 变量替换键值对
     * @return 用于命令快捷返回，恒返回 true
     */
    @SafeVarargs
    public final boolean t(CommandSender receiver, Pair<String, Object>... replacements) {
        ColorHelper.parseAndSend(receiver, str(replacements));
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param receiver 消息接收者
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tm(CommandSender receiver) {
        AdventureUtil.sendMessage(receiver, str());
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param receiver 消息接收者
     * @param args <code>String.format</code> 参数
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tm(CommandSender receiver, Object... args) {
        AdventureUtil.sendMessage(receiver, str(args));
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param receiver 消息接收者
     * @param replacements 变量替换键值对
     * @return 用于命令快捷返回，恒返回 true
     */
    @SafeVarargs
    public final boolean tm(CommandSender receiver, Pair<String, Object>... replacements) {
        AdventureUtil.sendMessage(receiver, str(replacements));
        return true;
    }
}
