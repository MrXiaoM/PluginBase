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
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        protected boolean libraries;

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
        public boolean libraries() {
            return libraries;
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
        protected boolean libraries;
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
                libraries = builder.libraries;
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
        public OptionsBuilder libraries(boolean libraries) {
            this.libraries = libraries;
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
    private URLClassLoader librariesClassLoader;
    public BukkitPlugin(OptionsBuilder builder) {
        this(builder.build());
    }
    private BukkitPlugin(Options options) {
        if (className.equals("group.pluginbase.BukkitPlugin".replace("group", "top.mrxiaom"))) {
            throw new IllegalStateException("PluginBase 依赖没有 relocate 到插件包，插件无法正常工作，请联系开发者解决该问题\n参考文档: https://github.com/MrXiaoM/PluginBase");
        }
        this.options = options;
        if (this.options.libraries()) {
            loadLibraries();
        }
    }

    protected void loadLibraries() {
        File librariesFolder = new File(getDataFolder(), "libraries");
        if (!librariesFolder.exists()) {
            Util.mkdirs(librariesFolder);
            return;
        }
        List<URL> urls = new ArrayList<>();
        File[] files = librariesFolder.listFiles();
        if (files != null) for (File file : files) try {
            urls.add(file.toURI().toURL());
        } catch (Throwable ignored) {
        }
        if (urls.isEmpty()) {
            librariesClassLoader = null;
        } else {
            URL[] array = new URL[urls.size()];
            for (int i = 0; i < urls.size(); i++) {
                array[i] = urls.get(i);
            }
            librariesClassLoader = new URLClassLoader(array, super.getClassLoader());
            urls.clear();
        }
    }

    public ClassLoader classLoader() {
        if (librariesClassLoader == null) {
            return super.getClassLoader();
        } else {
            return librariesClassLoader;
        }
    }

    public Connection getConnection() {
        return options.databaseHolder == null ? null : options.databaseHolder.getConnection();
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

    private void earlyLoadModules() {
        // 内部模块属于“依赖”的一部分，因为 scanIgnore 的存在，不能添加 @AutoRegister 注解来自动注册。
        // 应该在这里将内部模块手动添加到 earlyLoadModules 里面，进行独立的早期加载。
        // 并且由于部分内部模块应当是可选的，所以要额外加一层 try catch 以防相关类被精简导致报错。
        List<Class<? extends AbstractPluginHolder<?>>> earlyLoadModules = new ArrayList<>();
        try {
            Class<LanguageManager> languageManagerClass = LanguageManager.class;
            earlyLoadModules.add(languageManagerClass);
        } catch (Throwable ignored) {
        }
        try {
            Class<GuiManager> guiManagerClass = GuiManager.class;
            earlyLoadModules.add(guiManagerClass);
        } catch (Throwable ignored) {
        }

        loadModules(this, earlyLoadModules);
        earlyLoadModules.clear();
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

        pluginEnabled = true;
        earlyLoadModules();
        beforeEnable();
        loadModules(this, modulesToRegister);
        modulesToRegister.clear();

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

        if (librariesClassLoader != null) try {
            List<Driver> toRemove = new ArrayList<>();
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader().equals(librariesClassLoader)) {
                    toRemove.add(driver);
                }
            }
            for (Driver driver : toRemove) try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException ignored) {
            }
            toRemove.clear();
            librariesClassLoader.close();
        } catch (IOException ignored) {
        }
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
    
    public void saveResource(String path, File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            Util.mkdirs(parent);
        }
        try (InputStream resource = getResource(path)) {
            if (resource == null) return;
            try (FileOutputStream output = new FileOutputStream(file)) {
                int len;
                byte[] buffer = new byte[1024];
                while ((len = resource.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            warn("保存资源文件 " + path + " 时出错", e);
        }
    }

    public File resolve(String path) {
        return path.startsWith("./") ? new File(getDataFolder(), path.substring(2)) : new File(path);
    }
}
