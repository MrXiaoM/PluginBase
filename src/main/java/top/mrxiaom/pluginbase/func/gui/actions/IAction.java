package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Pair;

public interface IAction {
    void run(Player player, Pair<String, Object>[] replacements);
}
