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
    void cancelTasks();
}
