package top.mrxiaom.pluginbase.actions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;

import java.util.List;

public class ActionBroadcastMessageAdventure implements IAction {
    public static final IActionProvider PROVIDER = input -> {
        if (input instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) input;
            if (!section.contains("type") && section.contains("broadcast")) {
                String content = section.getString("broadcast");
                if (content != null) {
                    return new ActionBroadcastMessageAdventure(content);
                }
            } else if ("broadcast".equals(section.getString("type"))) {
                String content = section.getString("content");
                if (content != null) {
                    return new ActionBroadcastMessageAdventure(content);
                }
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[broadcast]")) {
                return new ActionBroadcastMessageAdventure(s.substring(11));
            }
            if (s.startsWith("broadcast:")) {
                return new ActionBroadcastMessageAdventure(s.substring(10));
            }
        }
        return null;
    };
    public final String message;
    public ActionBroadcastMessageAdventure(String message) {
        this.message = message;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        String str;
        if (player != null) {
            str = PAPI.setPlaceholders(player, Pair.replace(message, replacements));
        } else {
            str = Pair.replace(message, replacements);
        }
        AdventureUtil.sendMessage(Bukkit.getConsoleSender(), str);
        for (Player p : Bukkit.getOnlinePlayers()) {
            AdventureUtil.sendMessage(p, str);
        }
    }
}
