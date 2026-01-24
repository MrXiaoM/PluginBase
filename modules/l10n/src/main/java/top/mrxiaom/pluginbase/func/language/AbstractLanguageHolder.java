package top.mrxiaom.pluginbase.func.language;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static top.mrxiaom.pluginbase.utils.Pair.replace;

/**
 * 持有本地化键与默认值的抽象容器
 */
public abstract class AbstractLanguageHolder {
    private String key;
    /**
     * 默认值是否为列表
     */
    public final boolean isList;
    /**
     * 默认值
     */
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

    protected void key(String key) {
        this.key = key;
    }

    /**
     * 配置键，按 <code>.</code> 分隔
     */
    public String key() {
        return key;
    }

    /**
     * 获取本地化管理器
     */
    public abstract LanguageManager getLanguageManager();

    @SuppressWarnings({"unchecked"})
    private <T> T getOrDefault(T value) {
        return value == null ? (T) defaultValue : value;
    }

    /**
     * 获取本地化字符串
     */
    public String str() {
        LanguageManager lang = getLanguageManager();
        if (isList) {
            List<String> list = getOrDefault(lang.getAsList(key));
            return String.join("\n", list);
        } else {
            return getOrDefault(lang.getAsString(key));
        }
    }
    /**
     * 获取本地化字符串，并通过 <code>String.format</code> 进行格式化
     * @param args 参数列表
     * @see String#format(String, Object...)
     */
    public String strFormat(Object... args) {
        Object[] arguments = new Object[args.length];
        ILanguageArgumentProcessor processor = getLanguageManager().getProcessor();
        for (int i = 0; i < args.length; i++) {
            arguments[i] = processor.execute(this, null, args[i]);
        }
        return String.format(str(), arguments);
    }
    /**
     * 获取本地化字符串，并替换变量
     * @param replacements 替换变量列表
     */
    @SafeVarargs
    public final String str(Pair<String, Object>... replacements) {
        List<Pair<String, Object>> list = new ArrayList<>();
        ILanguageArgumentProcessor processor = getLanguageManager().getProcessor();
        for (Pair<String, Object> pair : replacements) {
            list.add(Pair.of(pair.key(), processor.execute(this, pair.key(), pair.value())));
        }
        return replace(str(), list);
    }
    /**
     * 获取本地化字符串，并替换变量
     * @param replacements 替换变量列表
     */
    public String str(Iterable<Pair<String, Object>> replacements) {
        List<Pair<String, Object>> list = new ArrayList<>();
        ILanguageArgumentProcessor processor = getLanguageManager().getProcessor();
        for (Pair<String, Object> pair : replacements) {
            list.add(Pair.of(pair.key(), processor.execute(this, pair.key(), pair.value())));
        }
        return replace(str(), list);
    }
    /**
     * 获取本地化字符串列表
     */
    public List<String> list() {
        LanguageManager lang = getLanguageManager();
        if (isList) {
            return getOrDefault(lang.getAsList(key));
        } else {
            String str = getOrDefault(lang.getAsString(key));
            return Lists.newArrayList(str.split("\n"));
        }
    }
    /**
     * 获取本地化字符串，并通过 <code>String.format</code> 进行格式化
     * @param args 参数列表
     * @see String#format(String, Object...)
     */
    public List<String> listFormat(Object... args) {
        Object[] arguments = new Object[args.length];
        ILanguageArgumentProcessor processor = getLanguageManager().getProcessor();
        for (int i = 0; i < args.length; i++) {
            arguments[i] = processor.execute(this, null, args[i]);
        }
        String formatted = String.format(String.join("\n", list()), arguments);
        return Lists.newArrayList(formatted.split("\n"));
    }
    /**
     * 获取本地化字符串列表，并替换变量
     * @param replacements 替换变量列表
     */
    @SafeVarargs
    public final List<String> list(Pair<String, Object>... replacements) {
        List<Pair<String, Object>> list = new ArrayList<>();
        ILanguageArgumentProcessor processor = getLanguageManager().getProcessor();
        for (Pair<String, Object> pair : replacements) {
            list.add(Pair.of(pair.key(), processor.execute(this, pair.key(), pair.value())));
        }
        return list().stream()
                .map(it -> replace(it, list))
                .collect(Collectors.toList());
    }
    /**
     * 获取本地化字符串列表，并替换变量
     * @param replacements 替换变量列表
     */
    public List<String> list(Iterable<Pair<String, Object>> replacements) {
        List<Pair<String, Object>> list = new ArrayList<>();
        ILanguageArgumentProcessor processor = getLanguageManager().getProcessor();
        for (Pair<String, Object> pair : replacements) {
            list.add(Pair.of(pair.key(), processor.execute(this, pair.key(), pair.value())));
        }
        return list().stream()
                .map(it -> replace(it, list))
                .collect(Collectors.toList());
    }

    /**
     * 发送消息
     * @param receiver 消息接收者
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean t(CommandSender receiver) {
        String message = str();
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }
    /**
     * 发送消息
     * @param receiver 消息接收者
     * @param args <code>String.format</code> 参数
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tf(CommandSender receiver, Object... args) {
        String message = strFormat(args);
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
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
        String message = str(replacements);
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }
    /**
     * 发送消息
     * @param receiver 消息接收者
     * @param replacements 变量替换键值对
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean t(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param receiver 消息接收者
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tm(CommandSender receiver) {
        String message = str();
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param miniMessage 自定义 MiniMessage 实例
     * @param receiver 消息接收者
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tm(MiniMessage miniMessage, CommandSender receiver) {
        String message = str();
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param receiver 消息接收者
     * @param args <code>String.format</code> 参数
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tmf(CommandSender receiver, Object... args) {
        String message = strFormat(args);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param miniMessage 自定义 MiniMessage 实例
     * @param receiver 消息接收者
     * @param args <code>String.format</code> 参数
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tmf(MiniMessage miniMessage, CommandSender receiver, Object... args) {
        String message = strFormat(args);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
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
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param miniMessage 自定义 MiniMessage 实例
     * @param receiver 消息接收者
     * @param replacements 变量替换键值对
     * @return 用于命令快捷返回，恒返回 true
     */
    @SafeVarargs
    public final boolean tm(MiniMessage miniMessage, CommandSender receiver, Pair<String, Object>... replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param receiver 消息接收者
     * @param replacements 变量替换键值对
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tm(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }
    /**
     * 以 MiniMessage 格式发送消息
     * @param miniMessage 自定义 MiniMessage 实例
     * @param receiver 消息接收者
     * @param replacements 变量替换键值对
     * @return 用于命令快捷返回，恒返回 true
     */
    public boolean tm(MiniMessage miniMessage, CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }
}
