package top.mrxiaom.pluginbase.utils.scheduler;

import io.papermc.paper.threadedregions.scheduler.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.api.IScheduler;
import top.mrxiaom.pluginbase.api.InventoryViewAccessor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FoliaScheduler implements IScheduler {
    private final AsyncScheduler asyncScheduler = Bukkit.getAsyncScheduler();
    private final GlobalRegionScheduler globalRegionScheduler = Bukkit.getGlobalRegionScheduler();
    private final RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
    private final BukkitPlugin plugin;
    public FoliaScheduler(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTask(@NotNull Runnable runnable) {
        globalRegionScheduler.run(plugin, (st) -> runnable.run());
    }

    @Override
    public @NotNull IRunTask runTaskLater(@NotNull Runnable runnable, long delay) {
        return wrap(globalRegionScheduler.runDelayed(plugin, (st) -> runnable.run(), delay));
    }

    @Override
    public @NotNull IRunTask runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
        return wrap(globalRegionScheduler.runAtFixedRate(plugin, (st) -> runnable.run(), delay, period));
    }

    @Override
    public @NotNull IRunTask runTaskAsync(@NotNull Runnable runnable) {
        return wrap(asyncScheduler.runNow(plugin, (st) -> runnable.run()));
    }

    @Override
    public @NotNull IRunTask runTaskLaterAsync(@NotNull Runnable runnable, long delay) {
        return wrap(asyncScheduler.runDelayed(plugin, (st) -> runnable.run(), delay * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public @NotNull IRunTask runTaskTimerAsync(@NotNull Runnable runnable, long delay, long period) {
        return wrap(asyncScheduler.runAtFixedRate(plugin, (st) -> runnable.run(), delay * 50L, period * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public <T extends Entity> void runAtEntity(@NotNull T entity, @NotNull Consumer<T> runnable) {
        EntityScheduler scheduler = entity.getScheduler();
        scheduler.run(plugin, (st) -> runnable.accept(entity), null);
    }

    @Override
    public @NotNull <T extends Entity> IRunTask runAtEntityLater(@NotNull T entity, @NotNull Consumer<T> runnable, long delay) {
        EntityScheduler scheduler = entity.getScheduler();
        return wrap(scheduler.runDelayed(plugin, (st) -> runnable.accept(entity), null, delay));
    }

    @Override
    public @NotNull <T extends Entity> IRunTask runAtEntityTimer(@NotNull T entity, @NotNull Consumer<T> runnable, long delay, long period) {
        EntityScheduler scheduler = entity.getScheduler();
        return wrap(scheduler.runAtFixedRate(plugin, (st) -> runnable.accept(entity), null, delay, period));
    }

    @Override
    public void runAtLocation(@NotNull Location location, @NotNull Consumer<Location> runnable) {
        regionScheduler.run(plugin, location, (st) -> runnable.accept(location));
    }

    @Override
    public @NotNull IRunTask runAtLocationLater(@NotNull Location location, @NotNull Consumer<Location> runnable, long delay) {
        return wrap(regionScheduler.runDelayed(plugin, location, (st) -> runnable.accept(location), delay));
    }

    @Override
    public @NotNull IRunTask runAtLocationTimer(@NotNull Location location, @NotNull Consumer<Location> runnable, long delay, long period) {
        return wrap(regionScheduler.runAtFixedRate(plugin, location, (st) -> runnable.accept(location), delay, period));
    }

    @Override
    public void teleport(@NotNull Entity entity, @NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause, @Nullable Consumer<Entity> then) {
        CompletableFuture<Boolean> future = entity.teleportAsync(location, cause);
        if (then != null) future.thenRun(() -> then.accept(entity));
    }

    @Override
    public void teleport(@NotNull Entity entity, @NotNull Location location, @Nullable Consumer<Entity> then) {
        CompletableFuture<Boolean> future = entity.teleportAsync(location);
        if (then != null) future.thenRun(() -> then.accept(entity));
    }

    @Override
    public void openInventory(HumanEntity player, Inventory inv) {
        runAtEntity(player, () -> player.openInventory(inv));
    }

    @Override
    public void openInventory(HumanEntity player, InventoryViewAccessor view) {
        runAtEntity(player, () -> view.openInventory(player));
    }

    @Override
    public void closeInventory(HumanEntity player) {
        runAtEntity(player, () -> player.closeInventory());
    }

    @Override
    public void cancelTasks() {
        globalRegionScheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }

    @NotNull
    private FoliaTask wrap(@Nullable ScheduledTask task) {
        return new FoliaTask(task);
    }

    public static class FoliaTask implements IRunTask {
        private final ScheduledTask task;
        public FoliaTask(@Nullable ScheduledTask task) {
            this.task = task;
        }

        @Nullable
        public ScheduledTask task() {
            return task;
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }
    }
}
