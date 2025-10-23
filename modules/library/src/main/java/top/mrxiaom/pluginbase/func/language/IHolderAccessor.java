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
     * @see AbstractLanguageHolder#str(Object...)
     */
    default String str(Object... args) {
        return holder().str(args);
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
     * @see AbstractLanguageHolder#list(Object...)
     */
    default List<String> list(Object... args) {
        return holder().list(args);
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
        ColorHelper.parseAndSend(receiver, str());
        return true;
    }

    /**
     * @see AbstractLanguageHolder#t(CommandSender, Object...)
     */
    default boolean t(CommandSender receiver, Object... args) {
        ColorHelper.parseAndSend(receiver, str(args));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#t(CommandSender, Pair[])
     */
    default boolean t(CommandSender receiver, Pair... replacements) {
        ColorHelper.parseAndSend(receiver, str(replacements));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#t(CommandSender, Iterable)
     */
    default boolean t(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        ColorHelper.parseAndSend(receiver, str(replacements));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender)
     */
    default boolean tm(CommandSender receiver) {
        AdventureUtil.sendMessage(receiver, str());
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender)
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver) {
        AdventureUtil.sendMessage(receiver, miniMessage, str());
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender, Object...)
     */
    default boolean tm(CommandSender receiver, Object... args) {
        AdventureUtil.sendMessage(receiver, str(args));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender, Object...)
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver, Object... args) {
        AdventureUtil.sendMessage(receiver, miniMessage, str(args));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender, Pair[])
     */
    default boolean tm(CommandSender receiver, Pair... replacements) {
        AdventureUtil.sendMessage(receiver, str(replacements));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender, Pair[])
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver, Pair... replacements) {
        AdventureUtil.sendMessage(receiver, miniMessage, str(replacements));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(CommandSender, Iterable)
     */
    default boolean tm(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        AdventureUtil.sendMessage(receiver, str(replacements));
        return true;
    }

    /**
     * @see AbstractLanguageHolder#tm(MiniMessage, CommandSender, Iterable)
     */
    default boolean tm(MiniMessage miniMessage, CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        AdventureUtil.sendMessage(receiver, miniMessage, str(replacements));
        return true;
    }
}
