package top.mrxiaom.pluginbase.utils.scheduler;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.api.IScheduler;
import top.mrxiaom.pluginbase.api.InventoryViewAccessor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FoliaLibScheduler implements IScheduler {
    private final FoliaLib foliaLib;
    private final PlatformScheduler scheduler;
    public FoliaLibScheduler(BukkitPlugin plugin) {
        this(plugin, null);
    }
    public FoliaLibScheduler(BukkitPlugin plugin, Consumer<FoliaLib> consumer) {
        this.foliaLib = new FoliaLib(plugin);
        this.scheduler = foliaLib.getScheduler();
        if (consumer != null) consumer.accept(foliaLib);
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public Task wrap(WrappedTask task) {
        return new Task(task);
    }

    @Override
    public void runTask(@NotNull Runnable runnable) {
        scheduler.runNextTick((t) -> runnable.run());
    }

    @Override
    public void runTaskJoin(@NotNull Runnable runnable) {
        scheduler.runNextTick((t) -> runnable.run()).join();
    }

    @Override
    public @NotNull IRunTask runTaskLater(@NotNull Runnable runnable, long delay) {
        return wrap(scheduler.runLater(runnable, delay));
    }

    @Override
    public @NotNull IRunTask runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
        return wrap(scheduler.runTimer(runnable, delay, period));
    }

    @Override
    public @NotNull IRunTask runTaskAsync(@NotNull Runnable runnable) {
        scheduler.runAsync((t) -> runnable.run());
        return DummyTask.INSTANCE;
    }

    @Override
    public @NotNull IRunTask runTaskLaterAsync(@NotNull Runnable runnable, long delay) {
        return wrap(scheduler.runLaterAsync(runnable, delay));
    }

    @Override
    public @NotNull IRunTask runTaskTimerAsync(@NotNull Runnable runnable, long delay, long period) {
        return wrap(scheduler.runTimerAsync(runnable, delay, period));
    }

    @Override
    public <T extends Entity> void runAtEntity(@NotNull T entity, @NotNull Consumer<T> runnable) {
        scheduler.runAtEntity(entity, wt -> runnable.accept(entity));
    }

    @Override
    public <T extends Entity> void runAtEntityJoin(@NonNull T entity, @NotNull Consumer<T> runnable) {
        scheduler.runAtEntity(entity, wt -> runnable.accept(entity)).join();
    }

    @Override
    public <T extends Entity> @NotNull IRunTask runAtEntityLater(@NotNull T entity, @NotNull Consumer<T> runnable, long delay) {
        return wrap(scheduler.runAtEntityLater(entity, () -> runnable.accept(entity), delay));
    }

    @Override
    public <T extends Entity> @NotNull IRunTask runAtEntityTimer(@NotNull T entity, @NotNull Consumer<T> runnable, long delay, long period) {
        return wrap(scheduler.runAtEntityTimer(entity, () -> runnable.accept(entity), delay, period));
    }

    @Override
    public void runAtLocation(@NotNull Location location, @NotNull Consumer<Location> runnable) {
        scheduler.runAtLocation(location, wt -> runnable.accept(location));
    }

    @Override
    public void runAtLocationJoin(@NotNull Location location, @NotNull Consumer<Location> runnable) {
        scheduler.runAtLocation(location, wt -> runnable.accept(location)).join();
    }

    @Override
    public @NotNull IRunTask runAtLocationLater(@NotNull Location location, @NotNull Consumer<Location> runnable, long delay) {
        return wrap(scheduler.runAtLocationLater(location, () -> runnable.accept(location), delay));
    }

    @Override
    public @NotNull IRunTask runAtLocationTimer(@NotNull Location location, @NotNull Consumer<Location> runnable, long delay, long period) {
        return wrap(scheduler.runAtLocationTimer(location, () -> runnable.accept(location), delay, period));
    }

    @Override
    public void teleport(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause, @Nullable Consumer<Entity> then) {
        CompletableFuture<Boolean> future = scheduler.teleportAsync(entity, location, cause);
        if (then != null) future.thenRun(() -> then.accept(entity));
    }

    @Override
    public void teleport(@NotNull Entity entity, @NotNull Location location, @Nullable Consumer<Entity> then) {
        CompletableFuture<Boolean> future = scheduler.teleportAsync(entity, location);
        if (then != null) future.thenRun(() -> then.accept(entity));
    }

    @Override
    public void openInventory(HumanEntity player, Inventory inv) {
        // Fuck Folia
        if (foliaLib.isFolia()) {
            scheduler.runAtEntity(player, t -> player.openInventory(inv)).join();
        } else {
            player.openInventory(inv);
        }
    }

    @Override
    public void openInventory(HumanEntity player, InventoryViewAccessor view) {
        // Fuck Folia
        if (foliaLib.isFolia()) {
            scheduler.runAtEntity(player, t -> view.openInventory(player)).join();
        } else {
            view.openInventory(player);
        }
    }

    @Override
    public void closeInventory(HumanEntity player) {
        // Fuck Folia
        if (foliaLib.isFolia()) {
            scheduler.runAtEntity(player, t -> player.closeInventory()).join();
        } else {
            player.closeInventory();
        }
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
