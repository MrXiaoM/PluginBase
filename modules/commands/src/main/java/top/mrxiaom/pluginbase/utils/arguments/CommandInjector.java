package top.mrxiaom.pluginbase.utils.arguments;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Spigot/Paper 命令表注入工具
 */
public class CommandInjector {
    private final Method methodSyncCommand;
    private final SimpleCommandMap commandMap;
    private final Map<String, Command> knownCommands;
    private CommandInjector() throws ReflectiveOperationException {
        Method methodCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
        methodCommandMap.setAccessible(true);
        methodCommandMap.invoke(Bukkit.getServer());

        this.methodSyncCommand = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
        this.methodSyncCommand.setAccessible(true);

        this.commandMap = (SimpleCommandMap) methodCommandMap.invoke(Bukkit.getServer());
        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
        field.setAccessible(true);
        // noinspection unchecked
        this.knownCommands = (Map<String, Command>) field.get(commandMap);
    }

    /**
     * 获取 Bukkit 命令表
     */
    @NotNull
    public SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    /**
     * 获取已注册的命令，可以直接对其进行操作修改
     */
    @NotNull
    public Map<String, Command> getKnownCommands() {
        return knownCommands;
    }

    /**
     * 注册简单命令 {@link Command#getLabel()}
     * <p>
     * 如果需要注册 <code>/fallbackPrefix:command</code> 命令，请考虑使用 {@link SimpleCommandMap#register(String, Command)}
     * @param command 命令实例
     * @param replace 如果存在冲突，是否替换原有命令
     * @return 是否注册成功
     */
    public boolean registerLabel(@NotNull Command command, boolean replace) {
        boolean edit = false;
        String label = command.getLabel();
        if (replace || !knownCommands.containsKey(label)) {
            knownCommands.put(label, command);
            edit = true;
        }
        if (edit) {
            command.register(commandMap);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 注册简单命令 {@link Command#getAliases()}
     * <p>
     * 如果需要注册 <code>/fallbackPrefix:command</code> 命令，请考虑使用 {@link SimpleCommandMap#register(String, Command)}
     * @param command 命令实例
     * @param replace 如果存在冲突，是否替换原有命令
     * @return 是否注册成功
     */
    public boolean registerAliases(@NotNull Command command, boolean replace) {
        boolean edit = false;
        for (String alias : command.getAliases()) {
            if (replace || !knownCommands.containsKey(alias)) {
                knownCommands.put(alias, command);
                edit = true;
            }
        }
        if (edit) {
            command.register(commandMap);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 注销简单命令 {@link Command#getLabel()}
     * @param command 命令实例
     * @return 是否注销成功
     */
    public boolean unregisterLabel(@NotNull Command command) {
        String label = command.getLabel();
        command.unregister(commandMap);
        return knownCommands.remove(label, command);
    }

    /**
     * 注销简单命令 {@link Command#getAliases()}
     * @param command 命令实例
     * @return 是否注销成功
     */
    public boolean unregisterAliases(@NotNull Command command) {
        boolean edit = false;
        command.unregister(commandMap);
        for (String alias : command.getAliases()) {
            if (knownCommands.remove(alias, command)) {
                edit = true;
            }
        }
        return edit;
    }

    /**
     * 将命令表同步到 Minecraft 服务端，以刷新可用的命令
     * @return 是否刷新成功
     */
    public boolean syncCommands() {
        try {
            methodSyncCommand.invoke(Bukkit.getServer());
            return true;
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    @NotNull
    public static CommandInjector create() throws ReflectiveOperationException {
        return new CommandInjector();
    }
}
