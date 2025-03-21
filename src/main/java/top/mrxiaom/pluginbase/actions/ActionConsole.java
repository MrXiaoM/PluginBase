package top.mrxiaom.pluginbase.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

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
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(command, replacements);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PAPI.setPlaceholders(player, s));
    }
}
