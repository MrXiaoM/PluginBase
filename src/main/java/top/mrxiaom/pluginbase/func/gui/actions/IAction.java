package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public interface IAction {
    default void run(Player player) {
        run(player, null);
    }
    void run(Player player, @Nullable List<Pair<String, Object>> replacements);
}
