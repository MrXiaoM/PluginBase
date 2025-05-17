package top.mrxiaom.pluginbase.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

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
    public ActionMessageAdventure(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(message, replacements);
        if (player != null) {
            AdventureUtil.sendMessage(player, PAPI.setPlaceholders(player, s));
        } else {
            AdventureUtil.sendMessage(Bukkit.getConsoleSender(), s);
        }
    }
}
