package top.mrxiaom.pluginbase.func;

import top.mrxiaom.pluginbase.BukkitPlugin;

public abstract class AbstractModule extends AbstractPluginHolder {
    public AbstractModule(BukkitPlugin plugin) {
        super(plugin, true);
    }
}
