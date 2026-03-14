package top.mrxiaom.pluginbase.actions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionConsole implements IAction {
    public static final IActionProvider PROVIDER = input -> {
        if (input instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) input;
            if (!section.contains("type") && section.contains("console")) {
                String command = section.getString("console");
                if (command != null) {
                    return new ActionConsole(command);
                }
            } else if ("console".equals(section.getString("type"))) {
                String command = section.getString("command");
                if (command != null) {
                    return new ActionConsole(command);
                }
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[console]")) {
                return new ActionConsole(s.substring(9));
            }
            if (s.startsWith("console:")) {
                return new ActionConsole(s.substring(8));
            }
        }
        return null;
    };
    public final String command;
    public ActionConsole(String command) {
        this.command = command;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        String commandLine = PAPI.setPlaceholders(player, Pair.replace(command, replacements));
        if (commandLine.trim().isEmpty()) {
            BukkitPlugin.getInstance().warn("无法运行操作 '[console]" + command + "'，其解析后的命令为空");
            return;
        }
        Util.dispatchCommand(Bukkit.getConsoleSender(), commandLine);
    }
}
