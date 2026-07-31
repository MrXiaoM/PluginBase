package top.mrxiaom.pluginbase.utils.scheduler;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IScheduler;
import top.mrxiaom.pluginbase.utils.Util;

public class Schedulers {
    public static IScheduler create(BukkitPlugin plugin) {
        if (Util.isFolia()) {
            return new FoliaScheduler(plugin);
        } else {
            return new BukkitScheduler(plugin);
        }
    }
}
