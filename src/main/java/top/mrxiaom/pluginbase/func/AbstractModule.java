package top.mrxiaom.pluginbase.func;

import top.mrxiaom.pluginbase.BukkitPlugin;

/**
 * 抽象功能模块，仅仅是会强制注册的 <code>AbstractPluginHolder</code>
 * @see top.mrxiaom.pluginbase.func.AbstractPluginHolder
 */
public abstract class AbstractModule<T extends BukkitPlugin> extends AbstractPluginHolder<T> {
    public AbstractModule(BukkitPlugin plugin) {
        super(plugin, true);
    }
}
