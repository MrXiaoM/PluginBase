package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

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
    private ActionMessage(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, Pair<String, Object>[] replacements) {
        String s = Pair.replace(message, replacements);
        t(player, PAPI.setPlaceholders(player, s));
    }
}
