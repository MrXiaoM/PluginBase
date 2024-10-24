package top.mrxiaom.pluginbase.func.language;

import org.bukkit.command.CommandSender;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface IHolderAccessor {

    AbstractLanguageHolder holder();
    
    default String str() {
        return holder().str();
    }
    default String str(Object... args) {
        return holder().str(args);
    }
    default String str(Pair... replacements) {
        return holder().str(replacements);
    }
    default List<String> list() {
        return holder().list();
    }
    default List<String> list(Object... args) {
        return holder().list(args);
    }
    default List<String> list(Pair... replacements) {
        Pair<String, Object>[] array = new Pair[replacements.length];
        for (int i = 0; i < replacements.length; i++) {
            array[i] = replacements[i].cast();
        }
        return holder().list(array);
    }
    default boolean t(CommandSender receiver) {
        ColorHelper.parseAndSend(receiver, str());
        return true;
    }
    default boolean t(CommandSender receiver, Object... args) {
        ColorHelper.parseAndSend(receiver, str(args));
        return true;
    }
    default boolean t(CommandSender receiver, Pair... replacements) {
        ColorHelper.parseAndSend(receiver, str(replacements));
        return true;
    }
    default boolean tm(CommandSender receiver) {
        AdventureUtil.sendMessage(receiver, str());
        return true;
    }
    default boolean tm(CommandSender receiver, Object... args) {
        AdventureUtil.sendMessage(receiver, str(args));
        return true;
    }
    default boolean tm(CommandSender receiver, Pair... replacements) {
        AdventureUtil.sendMessage(receiver, str(replacements));
        return true;
    }
}
