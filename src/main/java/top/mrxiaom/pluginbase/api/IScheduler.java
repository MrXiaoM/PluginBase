package top.mrxiaom.pluginbase.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
    <T extends Entity> void runAtEntity(T entity, Consumer<T> runnable);
    <T extends Entity> IRunTask runAtEntityLater(T entity, Consumer<T> runnable, long delay);
    <T extends Entity> IRunTask runAtEntityTimer(T entity, Consumer<T> runnable, long delay, long period);
    void runAtLocation(Location location, Consumer<Location> runnable);
    IRunTask runAtLocationLater(Location location, Consumer<Location> runnable, long delay);
    IRunTask runAtLocationTimer(Location location, Consumer<Location> runnable, long delay, long period);
    void teleport(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause, @Nullable Consumer<Entity> then);
    void teleport(Entity entity, Location location, @Nullable Consumer<Entity> then);
    default IRunTask runTaskAsynchronously(Runnable runnable) {
        return runTaskAsync(runnable);
    }
    default IRunTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskLaterAsync(runnable, delay);
    }
    default IRunTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        return runTaskTimerAsync(runnable, delay, period);
    }
    default <T extends Entity> void runAtEntity(T entity, Runnable runnable) {
        runAtEntity(entity, e -> runnable.run());
    }
    default <T extends Entity> IRunTask runAtEntityLater(T entity, Runnable runnable, long delay) {
        return runAtEntityLater(entity, e -> runnable.run(), delay);
    }
    default <T extends Entity> IRunTask runAtEntityTimer(T entity, Runnable runnable, long delay, long period) {
        return runAtEntityTimer(entity, e -> runnable.run(), delay, period);
    }
    default void runAtLocation(Location location, Runnable runnable) {
        runAtLocation(location, l -> runnable.run());
    }
    default IRunTask runAtLocationLater(Location location, Runnable runnable, long delay) {
        return runAtLocationLater(location, l -> runnable.run(), delay);
    }
    default IRunTask runAtLocationTimer(Location location, Runnable runnable, long delay, long period) {
        return runAtLocationTimer(location, l -> runnable.run(), delay, period);
    }

    void cancelTasks();
}
