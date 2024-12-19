package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

public class ActionPlayer implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[player]")) {
            return new ActionPlayer(s.substring(8));
        }
        if (s.startsWith("player:")) {
            return new ActionPlayer(s.substring(7));
        }
        return null;
    };
    public final String command;
    public ActionPlayer(String command) {
        this.command = ColorHelper.parseColor(command);
    }

    @Override
    public void run(Player player, Pair<String, Object>[] replacements) {
        String s = Pair.replace(command, replacements);
        Bukkit.dispatchCommand(player, PAPI.setPlaceholders(player, s));
    }
}
