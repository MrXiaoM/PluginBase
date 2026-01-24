package top.mrxiaom.pluginbase.func.language;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

/**
 * 用于快捷调用 AbstractLanguageHolder 中的方法，常用于 <code>enum</code>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public interface IHolderAccessor {

    AbstractLanguageHolder holder();

    /**
     * @see AbstractLanguageHolder#str()
     */
    default String str() {
        return holder().str();
    }

    /**
     * @see AbstractLanguageHolder#strFormat(Object...)
     */
    default String strFormat(Object... args) {
        return holder().strFormat(args);
    }

    /**
     * @see AbstractLanguageHolder#str(Pair[])
     */
    default String str(Pair... replacements) {
        return holder().str(replacements);
    }

    /**
     * @see AbstractLanguageHolder#str(Pair[])
     */
    default String str(Iterable<Pair<String, Object>> replacements) {
        return holder().str(replacements);
    }

    /**
     * @see AbstractLanguageHolder#list()
     */
    default List<String> list() {
        return holder().list();
    }

    /**
     * @see AbstractLanguageHolder#listFormat(Object...)
     */
    default List<String> listFormat(Object... args) {
        return holder().listFormat(args);
    }

    /**
     * @see AbstractLanguageHolder#list(Pair[])
     */
    default List<String> list(Pair... replacements) {
        Pair<String, Object>[] array = new Pair[replacements.length];
        for (int i = 0; i < replacements.length; i++) {
            array[i] = replacements[i].cast();
        }
        return holder().list(array);
    }

    /**
     * @see AbstractLanguageHolder#list(Iterable)
     */
    default List<String> list(Iterable<Pair<String, Object>> replacements) {
        return holder().list(replacements);
    }

    /**
     * @see AbstractLanguageHolder#t(CommandSender)
     */
    default boolean t(CommandSender receiver) {
        String message = str();
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tf(CommandSender, Object...)
     */
    default boolean tf(CommandSender receiver, Object... args) {
        String message = strFormat(args);
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#t(CommandSender, Pair[])
     */
    default boolean t(CommandSender receiver, Pair... replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#t(CommandSender, Iterable)
     */
    default boolean t(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            ColorHelper.parseAndSend(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender)
     */
    default boolean tm(CommandSender receiver) {
        String message = str();
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender)
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver) {
        String message = str();
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tmf(CommandSender, Object...)
     */
    default boolean tmf(CommandSender receiver, Object... args) {
        String message = strFormat(args);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tmf(MiniMessage, CommandSender, Object...)
     */
    default boolean tmf(MiniMessage miniMessage, CommandSender receiver, Object... args) {
        String message = strFormat(args);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender, Pair[])
     */
    default boolean tm(CommandSender receiver, Pair... replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender, Pair[])
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver, Pair... replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender, Iterable)
     */
    default boolean tm(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, message);
        }
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender, Iterable)
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        String message = str(replacements);
        if (!message.isEmpty()) {
            AdventureUtil.sendMessage(receiver, miniMessage, message);
        }
        return true;
    }
}
