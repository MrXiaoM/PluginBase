package top.mrxiaom.pluginbase.api;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface ICommandDispatcher {
    void dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine);
}
