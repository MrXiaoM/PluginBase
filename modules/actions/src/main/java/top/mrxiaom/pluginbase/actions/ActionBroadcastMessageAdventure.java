package top.mrxiaom.pluginbase.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
                    return new ActionBroadcastMessageAdventure(content, null, null, null);
                }
            } else if ("broadcast".equals(section.getString("type"))) {
                String content = section.getString("content");
                if (content != null) {
                    List<String> hoverLines = section.contains("hover") ? section.getStringList("hover") : null;
                    String clickAction = section.getString("click.action", "RUN_COMMAND");
                    String clickValue = section.getString("click.value", null);
                    return new ActionBroadcastMessageAdventure(content, hoverLines, clickAction, clickValue);
                }
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[broadcast]")) {
                return new ActionBroadcastMessageAdventure(s.substring(11), null, null, null);
            }
            if (s.startsWith("broadcast:")) {
                return new ActionBroadcastMessageAdventure(s.substring(10), null, null, null);
            }
        }
        return null;
    };
    public final @NotNull String message;
    public final @Nullable List<String> hoverLines;
    public final @Nullable String clickAction;
    public final @Nullable String clickValue;
    public ActionBroadcastMessageAdventure(@NotNull String message, @Nullable List<String> hoverLines, @Nullable String clickAction, @Nullable String clickValue) {
        this.message = message;
        this.hoverLines = hoverLines;
        this.clickAction = clickAction == null ? null : clickAction.replace("-", "_").toUpperCase();
        this.clickValue = clickValue;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        Component component;
        if (player != null) {
            String str = PAPI.setPlaceholders(player, Pair.replace(message, replacements));
            component = ActionMessageAdventure.parseComponent(str, hoverLines, clickAction, clickValue);
        } else {
            String str = Pair.replace(message, replacements);
            component = ActionMessageAdventure.parseComponent(str, hoverLines, clickAction, clickValue);
        }
        AdventureUtil.sendMessage(Bukkit.getConsoleSender(), component);
        for (Player p : Bukkit.getOnlinePlayers()) {
            AdventureUtil.sendMessage(p, component);
        }
    }
}
