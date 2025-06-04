package top.mrxiaom.pluginbase.utils.scheduler;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.api.IScheduler;

public class FoliaLibScheduler implements IScheduler {
    private final FoliaLib foliaLib;
    private final PlatformScheduler scheduler;
    public FoliaLibScheduler(BukkitPlugin plugin) {
        this.foliaLib = new FoliaLib(plugin);
        this.scheduler = foliaLib.getScheduler();
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public Task wrap(WrappedTask task) {
        return new Task(task);
    }

    @Override
    public IRunTask runTask(Runnable runnable) {
        scheduler.runNextTick((t) -> runnable.run());
        return DummyTask.INSTANCE;
    }

    @Override
    public IRunTask runTaskLater(Runnable runnable, long delay) {
        return wrap(scheduler.runLater(runnable, delay));
    }

    @Override
    public IRunTask runTaskTimer(Runnable runnable, long delay, long period) {
        return wrap(scheduler.runTimer(runnable, delay, period));
    }

    @Override
    public IRunTask runTaskAsync(Runnable runnable) {
        scheduler.runNextTick((t) -> runnable.run());
        return DummyTask.INSTANCE;
    }

    @Override
    public IRunTask runTaskLaterAsync(Runnable runnable, long delay) {
        return wrap(scheduler.runLaterAsync(runnable, delay));
    }

    @Override
    public IRunTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return wrap(scheduler.runTimerAsync(runnable, delay, period));
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelAllTasks();
    }

    public static class Task implements IRunTask {
        private final WrappedTask task;
        public Task(WrappedTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }
    }

    public static class DummyTask implements IRunTask {
        public static final DummyTask INSTANCE = new DummyTask();
        private DummyTask() {}
        @Override
        public void cancel() {
        }
    }
}
