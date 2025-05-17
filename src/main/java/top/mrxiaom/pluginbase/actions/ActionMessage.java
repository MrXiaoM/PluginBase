package top.mrxiaom.pluginbase.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class ActionMessage implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[message]")) {
            return new ActionMessage(s.substring(9));
        }
        if (s.startsWith("message:")) {
            return new ActionMessage(s.substring(8));
        }
        return null;
    };
    public final String message;
    public ActionMessage(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(message, replacements);
        if (player != null) {
            t(player, PAPI.setPlaceholders(player, s));
        } else {
            t(Bukkit.getConsoleSender(), s);
        }
    }
}
