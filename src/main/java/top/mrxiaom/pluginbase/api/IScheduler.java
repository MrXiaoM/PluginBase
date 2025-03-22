package top.mrxiaom.pluginbase.api;

/**
 * 方便对接 FoliaLib 的定时器接口
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IScheduler {
    IRunTask runTask(Runnable runnable);
    IRunTask runTaskLater(Runnable runnable, long delay);
    IRunTask runTaskTimer(Runnable runnable, long delay, long period);
    IRunTask runTaskAsync(Runnable runnable);
    IRunTask runTaskLaterAsync(Runnable runnable, long delay);
    IRunTask runTaskTimerAsync(Runnable runnable, long delay, long period);
    default IRunTask runTaskAsynchronously(Runnable runnable) {
        return runTaskAsync(runnable);
    }
    default IRunTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskLaterAsync(runnable, delay);
    }
    default IRunTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        return runTaskTimerAsync(runnable, delay, period);
    }
    void cancelTasks();
}
