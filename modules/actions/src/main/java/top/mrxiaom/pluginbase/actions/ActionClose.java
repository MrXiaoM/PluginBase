package top.mrxiaom.pluginbase.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionClose implements IAction {
    public static final IActionProvider PROVIDER;
    public static final ActionClose INSTANCE;
    static {
        INSTANCE = new ActionClose();
        PROVIDER = input -> {
            if (input instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) input;
                if ("close".equals(section.getString("type"))) {
                    return INSTANCE;
                }
            } else {
                String s = String.valueOf(input);
                if (s.equals("[close]") || s.equals("close")) {
                    return INSTANCE;
                }
            }
            return null;
        };
    }
    private ActionClose() {
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player != null) {
            player.closeInventory();
        }
    }
}
