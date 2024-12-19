package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

public class ActionActionBar implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[actionbar]")) {
            return new ActionActionBar(s.substring(11));
        }
        if (s.startsWith("actionbar:")) {
            return new ActionActionBar(s.substring(10));
        }
        return null;
    };
    public final String message;
    public ActionActionBar(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, Pair<String, Object>[] replacements) {
        String s = Pair.replace(message, replacements);
        AdventureUtil.sendActionBar(player, PAPI.setPlaceholders(player, s));
    }
}
