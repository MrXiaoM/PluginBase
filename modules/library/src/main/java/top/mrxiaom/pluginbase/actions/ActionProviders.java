package top.mrxiaom.pluginbase.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;

public class ActionProviders {
    private static final List<IActionProvider> actionProviders = new ArrayList<>();
    private ActionProviders() {}

    @NotNull
    public static List<IAction> loadActions(@NotNull ConfigurationSection section, @NotNull String key) {
        return loadActions(section.getStringList(key));
    }

    @NotNull
    public static List<IAction> loadActions(@NotNull ConfigurationSection section, @NotNull String... keys) {
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            list.addAll(section.getStringList(key));
        }
        if (list.isEmpty()) return new ArrayList<>();
        return loadActions(list);
    }

    @NotNull
    public static List<IAction> loadActions(@NotNull List<String> list) {
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
    public static IAction loadAction(@Nullable String s) {
        if (s == null) return null;
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

    public static void registerActionProviders(IActionProvider... providers) {
        registerActionProviders(Arrays.asList(providers));
    }

    public static void registerActionProviders(Collection<IActionProvider> providers) {
        actionProviders.addAll(providers);
        actionProviders.sort(Comparator.comparingInt(IActionProvider::priority));
    }

    public static void run(@NotNull BukkitPlugin plugin, @Nullable Player player, @NotNull List<IAction> actions) {
        run0(plugin, player, actions, null, 0);
    }

    public static void run(@NotNull BukkitPlugin plugin, @Nullable Player player, @NotNull List<IAction> actions, @Nullable List<Pair<String, Object>> replacements) {
        run0(plugin, player, actions, replacements, 0);
    }

    private static void run0(@NotNull BukkitPlugin plugin, @Nullable Player player, @NotNull List<IAction> actions, @Nullable List<Pair<String, Object>> replacements, int startIndex) {
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
