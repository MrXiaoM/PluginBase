package top.mrxiaom.pluginbase.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionPlayer implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[player]")) {
            return new ActionPlayer(s.substring(8));
        }
        if (s.startsWith("player:")) {
            return new ActionPlayer(s.substring(7));
        }
        return null;
    };
    public final String command;
    public ActionPlayer(String command) {
        this.command = command;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player != null) {
            String commandLine = PAPI.setPlaceholders(player, Pair.replace(command, replacements));
            if (commandLine.trim().isEmpty()) {
                BukkitPlugin.getInstance().warn("无法运行操作 '[player]" + command + "'，其解析后的命令为空");
                return;
            }
            Util.dispatchCommand(player, commandLine);
        }
    }
}
