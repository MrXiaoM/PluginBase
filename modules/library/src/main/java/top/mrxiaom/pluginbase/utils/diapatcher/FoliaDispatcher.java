package top.mrxiaom.pluginbase.utils.diapatcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.api.ICommandDispatcher;
import top.mrxiaom.pluginbase.api.IScheduler;

public class FoliaDispatcher implements ICommandDispatcher {
    private final IScheduler scheduler;
    public FoliaDispatcher(IScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) {
        if (sender instanceof Entity) {
            scheduler.runAtEntity((Entity) sender, () -> Bukkit.dispatchCommand(sender, commandLine));
        } else {
            Bukkit.dispatchCommand(sender, commandLine);
        }
    }
}
