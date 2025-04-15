package top.mrxiaom.pluginbase.func;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import static top.mrxiaom.pluginbase.utils.Util.stackTraceToString;

@SuppressWarnings({"unused", "unchecked"})
public abstract class AbstractPluginHolder<T extends BukkitPlugin> {
    private static final Map<Class<?>, AbstractPluginHolder<?>> registeredBungeeHolders = new HashMap<>();
    private static final Map<Class<?>, AbstractPluginHolder<?>> registeredHolders = new HashMap<>();
    public final T plugin;

    public AbstractPluginHolder(BukkitPlugin plugin) {
        this(plugin, false);
    }

    public AbstractPluginHolder(BukkitPlugin plugin, boolean register) {
        this.plugin = (T) plugin;
        if (register) register();
    }

    private static class HolderConstructor {
        private final Class<?> type;
        private final Constructor<?> constructor;
        private final int priority;
        public HolderConstructor(Class<?> type, Constructor<?> constructor, int priority) {
            this.type = type;
            this.constructor = constructor;
            this.priority = priority;
        }
        int priority() {
            return priority;
        }
        void execute(BukkitPlugin plugin) throws ReflectiveOperationException {
            constructor.newInstance(plugin);
        }
    }

    public static void loadModules(BukkitPlugin plugin, List<Class<? extends AbstractPluginHolder<?>>> classList) {
        List<HolderConstructor> list = new ArrayList<>();
        for (Class<?> type : classList) {
            try {
                AutoRegister meta = type.getAnnotation(AutoRegister.class);
                int priority;
                if (meta != null) {
                    boolean load = true;
                    for (String s : meta.requirePlugins()) {
                        if (!Bukkit.getPluginManager().isPluginEnabled(s)) {
                            load = false;
                            break;
                        }
                    }
                    if (!load) continue;
                    priority = meta.priority();
                } else {
                    priority = 1000;
                }
                Constructor<?> constructor;
                try {
                    constructor = type.getDeclaredConstructor(BukkitPlugin.class);
                } catch (Throwable t) {
                    constructor = type.getDeclaredConstructor(plugin.getClass());
                }
                list.add(new HolderConstructor(type, constructor, priority));
            } catch (Throwable t) {
                plugin.warn("读取模块 " + type.getName() + "时出现异常:", t);
            }
        }
        list.sort(Comparator.comparingInt(HolderConstructor::priority));
        for (HolderConstructor constructor : list) {
            try {
                constructor.execute(plugin);
            } catch (Throwable t) {
                plugin.warn("加载模块 " + constructor.type.getName() + "时出现异常:", t);
            }
        }
    }

    /**
     * 模块优先级。主要用于重载配置等操作
     * @return 优先级，数值越小越先执行
     */
    public int priority() {
        return 1000;
    }

    /**
     * 接收来自 BungeeCord 的 Forward 消息
     * @param subChannel 子频道
     * @param in 消息流
     */
    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {

    }

    public static void receiveFromBungee(String subChannel, byte[] bytes) {
        List<AbstractPluginHolder<?>> holders = new ArrayList<>(registeredBungeeHolders.values());
        holders.sort(Comparator.comparingInt(AbstractPluginHolder::priority));
        for (AbstractPluginHolder<?> holder : registeredBungeeHolders.values()) {
            try (DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(bytes))) {
                holder.receiveBungee(subChannel, msgIn);
            } catch (Throwable t) {
                BukkitPlugin.getInstance().warn("接收处理来自 BungeeCord 的消息时出现错误", t);
            }
        }
        holders.clear();
    }

    /**
     * 接收插件配置文件重载操作
     * @param config 配置文件 config.yml
     */
    public void reloadConfig(MemoryConfiguration config) {

    }

    public static void reloadAllConfig(MemoryConfiguration config) {
        List<AbstractPluginHolder<?>> holders = new ArrayList<>(registeredHolders.values());
        holders.sort(Comparator.comparingInt(AbstractPluginHolder::priority));
        for (AbstractPluginHolder<?> inst : holders) {
            inst.reloadConfig(config);
        }
        holders.clear();
    }

    public static Set<String> keys(ConfigurationSection section, String key) {
        ConfigurationSection s = section == null ? null : section.getConfigurationSection(key);
        return s == null ? new HashSet<>() : s.getKeys(false);
    }

    /**
     * 接收插件卸载时执行操作
     */
    public void onDisable() {

    }

    public static void callDisable() {
        List<AbstractPluginHolder<?>> holders = new ArrayList<>(registeredHolders.values());
        holders.sort(Comparator.comparingInt(AbstractPluginHolder::priority));
        for (AbstractPluginHolder<?> holder : holders) {
            holder.onDisable();
        }
        holders.clear();
        registeredHolders.clear();
        registeredBungeeHolders.clear();
    }

    /**
     * 注册事件监听器
     */
    protected void registerEvents() {
        if (this instanceof Listener) {
            registerEvents((Listener) this);
        }
    }

    /**
     * 注册事件监听器
     */
    protected void registerEvents(Listener listener) {
        try {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        } catch (Throwable t) {
            warn("在注册事件监听器 " + this.getClass().getSimpleName() + " 时出现一个异常", t);
        }
    }

    /**
     * 注册命令
     * @param label 命令名
     * @param inst 命令实例
     */
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

    /**
     * 从已注册的模块列表中寻找模块，找不到模块时，将返回 null
     */
    @Nullable
    @SuppressWarnings({"unchecked"})
    public static <T extends AbstractPluginHolder<?>> T getOrNull(Class<T> clazz) {
        return (T) registeredHolders.get(clazz);
    }

    /**
     * 从已注册的模块列表中寻找模块
     */
    @SuppressWarnings({"unchecked"})
    public static <T extends AbstractPluginHolder<?>> Optional<T> get(Class<T> clazz) {
        T inst = (T) registeredHolders.get(clazz);
        if (inst == null) return Optional.empty();
        return Optional.of(inst);
    }

    /**
     * 从已注册的模块列表中寻找模块，找不到模块时，将抛出一个异常
     */
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    protected static <T extends AbstractPluginHolder<?>> T instanceOf(Class<T> clazz) {
        T inst = (T) registeredHolders.get(clazz);
        if (inst == null) throw new IllegalStateException("无法找到已注册的 " + clazz.getName());
        return inst;
    }

    /**
     * 向玩家或控制台发送消息
     * @return 恒为 <code>true</code>
     */
    public static boolean t(CommandSender sender, String... msg) {
        ColorHelper.parseAndSend(sender, String.join("\n&r", msg));
        return true;
    }
}
