package top.mrxiaom.pluginbase.func;

import top.mrxiaom.pluginbase.BukkitPlugin;

public abstract class AbstractModule<T extends BukkitPlugin> extends AbstractPluginHolder<T> {
    public AbstractModule(BukkitPlugin plugin) {
        super(plugin, true);
    }
}
