package top.mrxiaom.pluginbase.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActionProviders {
    private static final List<IActionProvider> actionProviders = new ArrayList<>();
    private ActionProviders() {}

    @NotNull
    public static List<IAction> loadActions(ConfigurationSection section, String key) {
        return loadActions(section, new String[]{key});
    }

    @NotNull
    public static List<IAction> loadActions(ConfigurationSection section, String... keys) {
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            list.addAll(section.getStringList(key));
        }
        if (list.isEmpty()) return new ArrayList<>();
        return loadActions(list);
    }

    @NotNull
    public static List<IAction> loadActions(List<String> list) {
        List<IAction> actions = new ArrayList<>();
        for (String s : list) {
            IAction action = loadAction(s);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    @Nullable
    public static IAction loadAction(String s) {
        for (IActionProvider provider : actionProviders) {
            IAction action = provider.provide(s);
            if (action != null) return action;
        }
        return null;
    }

    public static void registerActionProvider(IActionProvider provider) {
        actionProviders.add(provider);
        actionProviders.sort(Comparator.comparingInt(IActionProvider::priority));
    }

    public static void run(BukkitPlugin plugin, Player player, List<IAction> actions) {
        run0(plugin, player, actions, null, 0);
    }

    public static void run(BukkitPlugin plugin, Player player, List<IAction> actions, @Nullable List<Pair<String, Object>> replacements) {
        run0(plugin, player, actions, replacements, 0);
    }

    private static void run0(BukkitPlugin plugin, Player player, List<IAction> actions, @Nullable List<Pair<String, Object>> replacements, int startIndex) {
        for (int i = startIndex; i < actions.size(); i++) {
            IAction action = actions.get(i);
            action.run(player, replacements);
            long delay = action.delayAfterRun();
            if (delay > 0) {
                int index = i + 1;
                plugin.getScheduler().runTaskLater(() -> run0(plugin, player, actions, replacements, index), delay);
                return;
            }
        }
    }
}
