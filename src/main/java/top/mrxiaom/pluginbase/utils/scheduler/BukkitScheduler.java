package top.mrxiaom.pluginbase.utils.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.api.IScheduler;

public class BukkitScheduler implements IScheduler {
    public static class Task implements IRunTask {
        BukkitTask impl;
        public Task(BukkitTask impl) {
            this.impl = impl;
        }
        @Override
        public void cancel() {
            impl.cancel();
        }
    }
    BukkitPlugin plugin;
    public BukkitScheduler(BukkitPlugin plugin) {
        this.plugin = plugin;
    }
    public Task wrap(BukkitTask task) {
        return new Task(task);
    }

    @Override
    public IRunTask runTask(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    @Override
    public IRunTask runTaskLater(Runnable runnable, long delay) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    @Override
    public IRunTask runTaskTimer(Runnable runnable, long delay, long period) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    @Override
    public IRunTask runTaskAsync(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public IRunTask runTaskLaterAsync(Runnable runnable, long delay) {
        return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    @Override
    public IRunTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return wrap(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
