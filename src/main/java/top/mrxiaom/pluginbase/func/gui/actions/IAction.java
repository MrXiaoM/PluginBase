package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Pair;

public interface IAction {
    default void run(Player player) {
        run(player, Pair.array(0));
    }
    void run(Player player, Pair<String, Object>[] replacements);
}
