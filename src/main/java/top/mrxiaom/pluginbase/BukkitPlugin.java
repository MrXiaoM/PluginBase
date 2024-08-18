package top.mrxiaom.pluginbase;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.func.AbstractPluginHolder;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.utils.Util;

import java.sql.Connection;
import java.util.*;
import java.util.logging.Level;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.*;

@SuppressWarnings({"unused"})
public abstract class BukkitPlugin extends JavaPlugin {
    public static class Options {
        protected boolean bungee;
        protected boolean database;
        protected boolean reconnectDatabaseWhenReloadConfig;
        protected boolean vaultEconomy;
        protected String scanPackage;
        protected List<String> scanIgnore;
        protected boolean adventure;

        protected DatabaseHolder databaseHolder;
        protected EconomyHolder economyHolder;
        private Options() {}

        private void enable(BukkitPlugin plugin) {
            if (database) {
                databaseHolder = new DatabaseHolder(plugin);
            }
            if (vaultEconomy) {
                economyHolder = new EconomyHolder(plugin);
            }
        }

        private void disable() {
            if (database && databaseHolder != null) {
                databaseHolder.close();
            }
        }

        public void registerDatabase(IDatabase... databases) {
            if (database && databaseHolder != null) {
                databaseHolder.registerDatabase(databases);
            }
        }

        public EconomyHolder economy() {
            return vaultEconomy ? economyHolder : null;
        }

        public DatabaseHolder database() {
            return database ? databaseHolder : null;
        }

        public boolean adventure() {
            return adventure;
        }
    }
    public static class OptionsBuilder {
        protected boolean bungee;
        protected boolean database;
        protected boolean reconnectDatabaseWhenReloadConfig;
        protected boolean vaultEconomy;
        protected String scanPackage = null;
        protected List<String> scanIgnore = new ArrayList<>();
        protected boolean adventure;
        private Options build() {
            return new Options() {{
                OptionsBuilder builder = OptionsBuilder.this;
                bungee = builder.bungee;
                database = builder.database;
                reconnectDatabaseWhenReloadConfig = builder.reconnectDatabaseWhenReloadConfig;
                vaultEconomy = builder.vaultEconomy;
                scanPackage = builder.scanPackage;
                scanIgnore = builder.scanIgnore;
                adventure = builder.adventure;
            }};
        }
        public OptionsBuilder bungee(boolean value) {
            this.bungee = value;
            return this;
        }
        public OptionsBuilder database(boolean value) {
            this.database = value;
            return this;
        }
        public OptionsBuilder reconnectDatabaseWhenReloadConfig(boolean value) {
            this.reconnectDatabaseWhenReloadConfig = value;
            return this;
        }
        public OptionsBuilder vaultEconomy(boolean value) {
            this.vaultEconomy = value;
            return this;
        }
        public OptionsBuilder scanPackage(String packageName) {
            this.scanPackage = packageName;
            return this;
        }
        public OptionsBuilder scanIgnore(Collection<String> packageNames) {
            scanIgnore.clear();
            scanIgnore.addAll(packageNames);
            return this;
        }
        public OptionsBuilder scanIgnore(String... packageNames) {
            return scanIgnore(Lists.newArrayList(packageNames));
        }
        public OptionsBuilder adventure(boolean adventure) {
            this.adventure = adventure;
            return this;
        }
    }
    protected static OptionsBuilder options() {
        return new OptionsBuilder();
    }
    private static final String className = BukkitPlugin.class.getName();
    private static BukkitPlugin instance;
    public static BukkitPlugin getInstance() {
        return instance;
    }
    private final List<Class<? extends AbstractPluginHolder<?>>> modulesToRegister = new ArrayList<>();
    private boolean pluginEnabled = false;
    public final Options options;
    private GuiManager guiManager = null;
    public BukkitPlugin(OptionsBuilder builder) {
        this(builder.build());
    }
    private BukkitPlugin(Options options) {
        if (className.equals("group.pluginbase.BukkitPlugin".replace("group", "top.mrxiaom"))) {
            throw new IllegalStateException("PluginBase 依赖没有 relocate 到插件包，插件无法正常工作，请联系开发者解决该问题\n参考文档: https://github.com/MrXiaoM/PluginBase");
        }
        this.options = options;
    }

    public Connection getConnection() {
        return options.databaseHolder == null ? null : options.databaseHolder.getConnection();
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    protected void beforeLoad() {

    }

    protected void afterLoad() {

    }

    protected void beforeEnable() {

    }

    protected void afterEnable() {

    }

    protected void beforeDisable() {

    }

    protected void afterDisable() {

    }

    @Override
    @Deprecated
    public void onLoad() {
        beforeLoad();
        afterLoad();
    }

    @Override
    @Deprecated
    @SuppressWarnings({"unchecked"})
    public void onEnable() {
        Util.init(instance = this);
        options.enable(this);

        if (options.vaultEconomy && options.economyHolder != null) {
            options.economyHolder.load();
        }

        String packageName = options.scanPackage != null ? options.scanPackage : getClass().getPackage().getName();
        Set<Class<?>> classes = Util.getClasses(getClassLoader(), packageName, options.scanIgnore);
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum()) continue;
            if (!AbstractPluginHolder.class.isAssignableFrom(clazz)) continue;

            AutoRegister annotation = clazz.getAnnotation(AutoRegister.class);
            if (annotation != null) {
                modulesToRegister.add((Class<? extends AbstractPluginHolder<?>>) clazz);
            }
        }

        beforeEnable();
        pluginEnabled = true;
        loadModules(this, modulesToRegister);
        modulesToRegister.clear();
        guiManager = new GuiManager(this);

        reloadConfig();

        if (options.bungee) {
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeIncomingChannel());
        }
        afterEnable();
    }

    @SafeVarargs
    protected final void registerModules(Class<? extends AbstractPluginHolder<?>>... classList) {
        if (pluginEnabled) {
            loadModules(this, Lists.newArrayList(classList));
        } else {
            modulesToRegister.addAll(Arrays.asList(classList));
        }
    }

    @Override
    @Deprecated
    public void onDisable() {
        beforeDisable();
        callDisable();
        if (options.bungee) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        }
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        options.disable();
        pluginEnabled = false;
        afterDisable();
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();

        FileConfiguration config = getConfig();

        if (options.database && options.databaseHolder != null) {
            options.databaseHolder.reloadConfig();
            if (options.reconnectDatabaseWhenReloadConfig) {
                options.databaseHolder.reconnect();
            }
        }

        reloadAllConfig(config);
    }

    public void warn(String msg, Throwable t) {
        getLogger().log(Level.WARNING, msg, t);
    }

    public void warn(String msg) {
        getLogger().log(Level.WARNING, msg);
    }
}
