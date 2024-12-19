package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

public class ActionMessageAdventure implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[message]")) {
            return new ActionMessageAdventure(s.substring(9));
        }
        if (s.startsWith("message:")) {
            return new ActionMessageAdventure(s.substring(8));
        }
        return null;
    };
    public final String message;
    private ActionMessageAdventure(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, Pair<String, Object>[] replacements) {
        String s = Pair.replace(message, replacements);
        AdventureUtil.sendMessage(player, PAPI.setPlaceholders(player, s));
    }
}
