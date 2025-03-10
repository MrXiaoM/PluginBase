package top.mrxiaom.pluginbase.actions;

import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActionProviders {
    private static final List<IActionProvider> actionProviders = new ArrayList<>();
    private ActionProviders() {}

    public static List<IAction> loadActions(ConfigurationSection section, String key) {
        List<String> list = section.getStringList(key);
        return loadActions(list);
    }

    public static List<IAction> loadActions(List<String> list) {
        List<IAction> actions = new ArrayList<>();
        for (String s : list) {
            for (IActionProvider provider : actionProviders) {
                IAction action = provider.provide(s);
                if (action != null) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    public static void registerActionProvider(IActionProvider provider) {
        actionProviders.add(provider);
        actionProviders.sort(Comparator.comparingInt(IActionProvider::priority));
    }
}
