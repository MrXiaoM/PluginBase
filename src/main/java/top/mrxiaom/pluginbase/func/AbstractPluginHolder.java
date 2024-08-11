package top.mrxiaom.pluginbase.func;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static top.mrxiaom.pluginbase.utils.Util.stackTraceToString;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder {
    private static final Map<Class<?>, AbstractPluginHolder> registeredBungeeHolders = new HashMap<>();
    private static final Map<Class<?>, AbstractPluginHolder> registeredHolders = new HashMap<>();
    public final BukkitPlugin plugin;

    public AbstractPluginHolder(BukkitPlugin plugin) {
        this(plugin, false);
    }

    public AbstractPluginHolder(BukkitPlugin plugin, boolean register) {
        this.plugin = plugin;
        if (register) register();
    }

    public static void loadModules(BukkitPlugin plugin, List<Class<? extends AbstractPluginHolder>> classList) {
        for (Class<?> clazz : classList) {
            try {
                clazz.getDeclaredConstructor(plugin.getClass()).newInstance(plugin);
            } catch (Throwable t) {
                plugin.warn("加载 " + clazz.getName() + "时出现异常:", t);
            }
        }
    }

    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {

    }

    public static void receiveFromBungee(String subChannel, byte[] bytes) {
        for (AbstractPluginHolder holder : registeredBungeeHolders.values()) {
            try (DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(bytes))) {
                holder.receiveBungee(subChannel, msgIn);
            } catch (Throwable t) {
                BukkitPlugin.getInstance().warn("接收处理来自 BungeeCord 的消息时出现错误", t);
            }
        }
    }

    public void reloadConfig(MemoryConfiguration config) {

    }

    public static void reloadAllConfig(MemoryConfiguration config) {
        for (AbstractPluginHolder inst : registeredHolders.values()) {
            inst.reloadConfig(config);
        }
    }

    public void onDisable() {

    }

    public static void callDisable() {
        for (AbstractPluginHolder holder : registeredHolders.values()) {
            holder.onDisable();
        }
        registeredHolders.clear();
        registeredBungeeHolders.clear();
    }

    protected void registerEvents() {
        if (this instanceof Listener) {
            registerEvents((Listener) this);
        }
    }

    protected void registerEvents(Listener listener) {
        try {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        } catch (Throwable t) {
            warn("在注册事件监听器 " + this.getClass().getSimpleName() + " 时出现一个异常", t);
        }
    }

    protected void registerCommand(String label, Object inst) {
        PluginCommand command = plugin.getCommand(label);
        if (command != null) {
            if (inst instanceof CommandExecutor) {
                command.setExecutor((CommandExecutor) inst);
            } else {
                warn(inst.getClass().getSimpleName() + " 不是一个命令执行器");
            }
            if (inst instanceof TabCompleter) {
                command.setTabCompleter((TabCompleter) inst);
            }
        } else {
            info("无法注册命令 /" + label);
        }
    }

    protected void register() {
        registeredHolders.put(getClass(), this);
    }

    protected void unregister() {
        registeredHolders.remove(getClass());
    }

    protected boolean isRegistered() {
        return registeredHolders.containsKey(getClass());
    }

    protected void registerBungee() {
        registeredBungeeHolders.put(getClass(), this);
    }

    protected void unregisterBungee() {
        registeredBungeeHolders.remove(getClass());
    }

    protected boolean isRegisteredBungee() {
        return registeredBungeeHolders.containsKey(getClass());
    }

    public void info(String... lines) {
        for (String line : lines) {
            plugin.getLogger().info(line);
        }
    }

    public void warn(String... lines) {
        for (String line : lines) {
            plugin.getLogger().warning(line);
        }
    }

    public void warn(Throwable t) {
        plugin.getLogger().warning(stackTraceToString(t));
    }

    public void warn(String s, Throwable t) {
        plugin.getLogger().warning(s);
        plugin.getLogger().warning(stackTraceToString(t));
    }

    @Nullable
    @SuppressWarnings({"unchecked"})
    public static <T extends AbstractPluginHolder> T getOrNull(Class<T> clazz) {
        return (T) registeredHolders.get(clazz);
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends AbstractPluginHolder> Optional<T> get(Class<T> clazz) {
        T inst = (T) registeredHolders.get(clazz);
        if (inst == null) return Optional.empty();
        return Optional.of(inst);
    }

    @SuppressWarnings({"unchecked"})
    protected static <T extends AbstractPluginHolder> T instanceOf(Class<T> clazz) {
        T inst = (T) registeredHolders.get(clazz);
        if (inst == null) throw new IllegalStateException("无法找到已注册的 " + clazz.getName());
        return inst;
    }

    public static boolean t(CommandSender sender, String... msg) {
        sender.sendMessage(ColorHelper.parseColor(String.join("\n&r", msg)));
        return true;
    }
}
