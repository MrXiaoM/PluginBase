package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

public class ActionConsole implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[console]")) {
            return new ActionConsole(s.substring(9));
        }
        if (s.startsWith("console:")) {
            return new ActionConsole(s.substring(8));
        }
        return null;
    };
    public final String command;
    public ActionConsole(String command) {
        this.command = ColorHelper.parseColor(command);
    }

    @Override
    public void run(Player player, Pair<String, Object>[] replacements) {
        String s = Pair.replace(command, replacements);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PAPI.setPlaceholders(player, s));
    }
}
