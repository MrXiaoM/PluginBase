package top.mrxiaom.pluginbase.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionPostProcessor;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.api.IRegistry;
import top.mrxiaom.pluginbase.data.SimpleRegistry;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;

public class ActionProviders {
    private static final IRegistry<IActionProvider> actionProviderRegistry = new SimpleRegistry<>();
    private static final IRegistry<IActionPostProcessor> actionPostProcessorRegistry = new SimpleRegistry<>();
    private static boolean registeredBuiltIn = false;
    private ActionProviders() {}

    public static IRegistry<IActionProvider> actionProviderRegistry() {
        return actionProviderRegistry;
    }

    public static IRegistry<IActionPostProcessor> actionPostProcessorRegistry() {
        return actionPostProcessorRegistry;
    }

    public static void registerBuiltInActions(BukkitPlugin plugin) {
        if (registeredBuiltIn) return;
        try {
            ActionProviders.registerActionProvider(ActionConsole.PROVIDER);
            ActionProviders.registerActionProvider(ActionPlayer.PROVIDER);
            if (plugin.options.adventure()) {
                ActionProviders.registerActionProvider(ActionActionBar.PROVIDER);
                ActionProviders.registerActionProvider(ActionMessageAdventure.PROVIDER);
                ActionProviders.registerActionProvider(ActionBroadcastMessageAdventure.PROVIDER);
                ActionProviders.registerActionProvider(ActionTitleAdventure.PROVIDER);
            } else {
                ActionProviders.registerActionProvider(ActionMessage.PROVIDER);
                ActionProviders.registerActionProvider(ActionBroadcastMessage.PROVIDER);
                ActionProviders.registerActionProvider(ActionTitle.PROVIDER);
            }
            ActionProviders.registerActionProvider(ActionSound.PROVIDER);
            ActionProviders.registerActionProvider(ActionClose.PROVIDER);
            ActionProviders.registerActionProvider(ActionDelay.PROVIDER);
        } catch (Throwable ignored) {
        }
        registeredBuiltIn = true;
    }

    @NotNull
    public static List<IAction> loadActions(@NotNull ConfigurationSection section, @NotNull String key) {
        if (section.contains(key)) {
            List<Object> list = ConfigUtils.getList(section, key);
            if (list.isEmpty()) return new ArrayList<>();
            return loadActions(list);
        } else {
            return new ArrayList<>();
        }
    }

    @NotNull
    public static List<IAction> loadActions(@NotNull ConfigurationSection section, @NotNull String... keys) {
        List<Object> list = new ArrayList<>();
        for (String key : keys) {
            if (section.contains(key)) {
                list.addAll(ConfigUtils.getList(section, key));
            }
        }
        if (list.isEmpty()) return new ArrayList<>();
        return loadActions(list);
    }

    @NotNull
    public static List<IAction> loadActions(@NotNull List<?> list) {
        List<IAction> actions = new ArrayList<>();
        for (Object input : list) {
            IAction action = loadAction(input);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    @Nullable
    public static IAction loadAction(@Nullable Object input) {
        if (input == null) return null;
        List<IActionProvider> allProviders = actionProviderRegistry.all();
        List<IActionPostProcessor> allPostProcessors = actionPostProcessorRegistry.all();
        for (IActionProvider provider : allProviders) {
            IAction action = provider.provide(input);
            if (action != null) {
                for (IActionPostProcessor processor : allPostProcessors) {
                    IAction processed = processor.process(input, provider, action);
                    if (processed != null) {
                        return processed;
                    }
                }
                return action;
            }
        }
        return null;
    }

    public static void registerActionProvider(IActionProvider provider) {
        actionProviderRegistry.register(provider);
    }

    public static void registerActionProviders(IActionProvider... providers) {
        for (IActionProvider provider : providers) {
            actionProviderRegistry.register(provider);
        }
    }

    public static void registerActionProviders(Collection<IActionProvider> providers) {
        for (IActionProvider provider : providers) {
            actionProviderRegistry.register(provider);
        }
    }

    public static void run(@NotNull BukkitPlugin plugin, @Nullable Player player, @NotNull List<IAction> actions) {
        run0(plugin, player, actions, null, 0);
    }

    public static void run(@NotNull BukkitPlugin plugin, @Nullable Player player, @NotNull List<IAction> actions, @Nullable List<Pair<String, Object>> replacements) {
        run0(plugin, player, actions, replacements, 0);
    }

    private static void run0(@NotNull BukkitPlugin plugin, @Nullable Player player, @NotNull List<IAction> actions, @Nullable List<Pair<String, Object>> replacements, int startIndex) {
        try {
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
        } catch (Throwable t) {
            plugin.warn("执行操作时出现异常", t);
            throw new RuntimeException(t);
        }
    }
}
