package top.mrxiaom.pluginbase.func.language;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;

public class LanguageManagerImpl extends LanguageManager {
    public LanguageManagerImpl(BukkitPlugin plugin) {
        super(plugin);
        register();
    }
}
