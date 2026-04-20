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
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionMessageAdventure implements IAction {
    public static final IActionProvider PROVIDER = input -> {
        if (input instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) input;
            if (!section.contains("type") && section.contains("message")) {
                String content = section.getString("message");
                if (content != null) {
                    return new ActionMessageAdventure(content, null, null, null);
                }
            } else if ("message".equals(section.getString("type"))) {
                String content = section.getString("content");
                if (content != null) {
                    List<String> hoverLines = section.contains("hover") ? section.getStringList("hover") : null;
                    String clickAction = section.getString("click.action", "RUN_COMMAND");
                    String clickValue = section.getString("click.value", null);
                    return new ActionMessageAdventure(content, hoverLines, clickAction, clickValue);
                }
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[message]")) {
                return new ActionMessageAdventure(s.substring(9), null, null, null);
            }
            if (s.startsWith("message:")) {
                return new ActionMessageAdventure(s.substring(8), null, null, null);
            }
        }
        return null;
    };
    public final @NotNull String message;
    public final @Nullable List<String> hoverLines;
    public final @Nullable String clickAction;
    public final @Nullable String clickValue;
    public ActionMessageAdventure(@NotNull String message, @Nullable List<String> hoverLines, @Nullable String clickAction, @Nullable String clickValue) {
        this.message = message;
        this.hoverLines = hoverLines;
        this.clickAction = clickAction == null ? null : clickAction.replace("-", "_").toUpperCase();
        this.clickValue = clickValue;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(message, replacements);
        Component component;
        if (player != null) {
            component = parseComponent(PAPI.setPlaceholders(player, Pair.replace(message, replacements)), hoverLines, clickAction, clickValue);
            AdventureUtil.sendMessage(player, component);
        } else {
            component = parseComponent(Pair.replace(message, replacements), hoverLines, clickAction, clickValue);
            AdventureUtil.sendMessage(Bukkit.getConsoleSender(), component);
        }
    }

    protected static Component parseComponent(String str, List<String> hoverLines, String clickAction, String clickValue) {
        Component component = AdventureUtil.miniMessage(str);
        if (hoverLines != null) {
            component = component.hoverEvent(AdventureUtil.miniMessageLines(hoverLines));
        }
        if (clickAction != null && clickValue != null) {
            switch (clickAction) {
                case "RUN_COMMAND":
                    component = component.clickEvent(ClickEvent.runCommand(clickValue));
                    break;
                case "SUGGEST_COMMAND":
                    component = component.clickEvent(ClickEvent.suggestCommand(clickValue));
                    break;
                case "COPY_TO_CLIPBOARD":
                    component = component.clickEvent(ClickEvent.copyToClipboard(clickValue));
                    break;
                case "OPEN_URL":
                    component = component.clickEvent(ClickEvent.openUrl(clickValue));
                    break;
                case "OPEN_FILE":
                    component = component.clickEvent(ClickEvent.openFile(clickValue));
                    break;
            }
        }
        return component;
    }
}
