package top.mrxiaom.pluginbase;

import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.*;
import top.mrxiaom.pluginbase.api.IScheduler;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.economy.EnumEconomy;
import top.mrxiaom.pluginbase.economy.IEconomy;
import top.mrxiaom.pluginbase.economy.NoEconomy;
import top.mrxiaom.pluginbase.economy.VaultEconomy;
import top.mrxiaom.pluginbase.func.AbstractPluginHolder;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.func.language.LanguageManagerImpl;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.item.LegacyItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.BukkitScheduler;

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
        protected EnumEconomy economyOption;
        protected boolean allowNoEconomy;
        protected String scanPackage;
        protected List<String> scanIgnore;
        protected boolean adventure;
        protected boolean libraries;
        protected boolean disableDefaultConfig;
        protected boolean enableConfigGotoFlag;

        protected DatabaseHolder databaseHolder;
        protected IEconomy economy;
        private Options() {}

        /**
         * @return 如果出现需要禁用插件的错误情况，返回 <code>true</code>
         */
        private boolean enable(BukkitPlugin plugin) {
            if (database) {
                databaseHolder = new DatabaseHolder(plugin);
            }
            switch (economyOption) {
                case VAULT: {
                    try {
                        RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
                        Economy provider = service == null ? null : service.getProvider();
                        if (provider != null) {
                            economy = new VaultEconomy(provider);
                            break;
                        }
                    } catch (NoClassDefFoundError ignored) {
                    }
                    if (allowNoEconomy) {
                        economy = NoEconomy.INSTANCE;
                    } else {
                        plugin.warnNoEconomy();
                        return true;
                    }
                    break;
                }
                case CUSTOM: {
                    IEconomy custom = plugin.initCustomEconomy();
                    if (custom != null) {
                        economy = custom;
                        break;
                    }
                    if (allowNoEconomy) {
                        economy = NoEconomy.INSTANCE;
                    } else {
                        plugin.warnNoEconomy();
                        return true;
                    }
                    break;
                }
                default: {
                    economy = NoEconomy.INSTANCE;
                    break;
                }
            }
            return false;
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

        public IEconomy economy() {
            return economy;
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
        protected EnumEconomy economy = EnumEconomy.NONE;
        protected boolean allowNoEconomy;
        protected String scanPackage = null;
        protected List<String> scanIgnore = new ArrayList<>();
        protected boolean adventure;
        protected boolean libraries;
        protected boolean disableDefaultConfig;
        protected boolean enableConfigGotoFlag;
        private Options build() {
            return new Options() {{
                OptionsBuilder builder = OptionsBuilder.this;
                bungee = builder.bungee;
                database = builder.database;
                reconnectDatabaseWhenReloadConfig = builder.reconnectDatabaseWhenReloadConfig;
                economyOption = builder.economy;
                allowNoEconomy = builder.allowNoEconomy;
                scanPackage = builder.scanPackage;
                scanIgnore = builder.scanIgnore;
                adventure = builder.adventure;
                libraries = builder.libraries;
                disableDefaultConfig = builder.disableDefaultConfig;
                enableConfigGotoFlag = builder.enableConfigGotoFlag;
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
        public OptionsBuilder economy(EnumEconomy value) {
            this.economy = value;
            return this;
        }
        public OptionsBuilder economy(EnumEconomy value, boolean allowNoEconomy) {
            this.economy = value;
            this.allowNoEconomy = allowNoEconomy;
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
        public OptionsBuilder disableDefaultConfig(boolean disableDefaultConfig) {
            this.disableDefaultConfig = disableDefaultConfig;
            return this;
        }
        public OptionsBuilder enableConfigGotoFlag(boolean enableConfigGotoFlag) {
            this.enableConfigGotoFlag = enableConfigGotoFlag;
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
    protected final ClassLoaderWrapper classLoader;
    protected IScheduler scheduler = new BukkitScheduler(this);
    public BukkitPlugin(OptionsBuilder builder) {
        this(builder.build());
    }
    private BukkitPlugin(Options options) {
        if (className.equals("group.pluginbase.BukkitPlugin".replace("group", "top.mrxiaom"))) {
            throw new IllegalStateException("PluginBase 依赖没有 relocate 到插件包，插件无法正常工作，请联系开发者解决该问题\n参考文档: https://github.com/MrXiaoM/PluginBase");
        }
        instance = this;
        this.options = options;
        this.classLoader = new ClassLoaderWrapper((URLClassLoader) getClassLoader());
        if (this.options.libraries() || this.options.database) {
            loadLibraries();
        }
    }

    @Nullable
    protected IEconomy initCustomEconomy() {
        return null;
    }

    @NotNull
    public ItemEditor initItemEditor() {
        return new LegacyItemEditor();
    }

    protected void warnNoEconomy() {
        warn("未发现经济接口实现，插件将卸载");
    }

    protected void loadLibraries() {
        File librariesFolder = new File(getDataFolder(), "libraries");
        if (!librariesFolder.exists()) {
            createLibrariesFolder(librariesFolder);
        }
        List<File> files = listLibraries(librariesFolder);
        for (File file : files) {
            if (file.isDirectory()) continue;
            try {
                URL url = file.toURI().toURL();
                this.classLoader.addURL(url);
                info("已加载依赖库 " + file.getName());
                afterLoadLib(file);
            } catch (Throwable t) {
                warn("无法加载依赖库 " + file.getName(), t);
            }
        }
    }

    protected void createLibrariesFolder(File folder) {
        if (options.libraries()) {
            Util.mkdirs(folder);
        }
    }

    protected List<File> listLibraries(File folder) {
        List<File> list = new ArrayList<>();
        File[] files = folder.isDirectory() ? folder.listFiles() : null;
        if (files != null) for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith(".jar")) continue;
            list.add(file);
        }
        return list;
    }

    protected void afterLoadLib(File file) {

    }

    public IScheduler getScheduler() {
        return scheduler;
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
            Class<LanguageManagerImpl> languageManagerClass = LanguageManagerImpl.class;
            earlyLoadModules.add(languageManagerClass);
        } catch (Throwable ignored) {
        }
        try {
            Class<GuiManager> guiManagerClass = GuiManager.class;
            earlyLoadModules.add(guiManagerClass);
        } catch (Throwable ignored) {
        }
        try {
            ActionProviders.registerActionProvider(ActionConsole.PROVIDER);
            ActionProviders.registerActionProvider(ActionPlayer.PROVIDER);
            if (options.adventure) {
                ActionProviders.registerActionProvider(ActionActionBar.PROVIDER);
                ActionProviders.registerActionProvider(ActionMessageAdventure.PROVIDER);
            } else {
                ActionProviders.registerActionProvider(ActionMessage.PROVIDER);
            }
            ActionProviders.registerActionProvider(ActionClose.PROVIDER);
            ActionProviders.registerActionProvider(ActionDelay.PROVIDER);
        } catch (Throwable ignored) {
        }

        loadModules(this, earlyLoadModules);
        earlyLoadModules.clear();
    }

    public Class<?> getConstructorType() {
        return getClass();
    }

    @Override
    @Deprecated
    @SuppressWarnings({"unchecked"})
    public void onEnable() {
        Util.init(this);
        if (options.enable(this)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
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

    protected final boolean hasScheduledRegisterModule(Class<? extends AbstractPluginHolder<?>> type) {
        return modulesToRegister.contains(type);
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
        scheduler.cancelTasks();
        options.disable();
        pluginEnabled = false;
        afterDisable();

        List<Driver> toRemove = new ArrayList<>();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader().equals(getClassLoader())) {
                toRemove.add(driver);
            }
        }
        for (Driver driver : toRemove) try {
            DriverManager.deregisterDriver(driver);
        } catch (SQLException ignored) {
        }
        toRemove.clear();
    }

    protected void beforeReloadConfig(FileConfiguration config) {

    }

    private FileConfiguration resolveGotoFlag(FileConfiguration last, int times) {
        if (times > 64) {
            warn("配置文件中的 goto 跳转次数过多，请自行检查 goto 标签是否有循环调用问题");
            return last;
        }
        String gotoFlag = last.getString("goto", null);
        if (gotoFlag != null) {
            File file = resolve(gotoFlag);
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(file);
            return resolveGotoFlag(newConfig, times + 1);
        }
        return last;
    }

    @Override
    public void reloadConfig() {
        FileConfiguration config;
        if (!options.disableDefaultConfig) {
            this.saveDefaultConfig();
            super.reloadConfig();
            config = getConfig();
            if (options.enableConfigGotoFlag) {
                config = resolveGotoFlag(config, 0);
            }
        } else {
            config = new YamlConfiguration();
        }

        beforeReloadConfig(config);

        if (options.database && options.databaseHolder != null) {
            options.databaseHolder.reloadConfig();
            if (options.reconnectDatabaseWhenReloadConfig) {
                options.databaseHolder.reconnect();
            }
        }

        reloadAllConfig(config);
    }

    public void info(String msg) {
        getLogger().log(Level.INFO, msg);
    }

    public void warn(String msg, Throwable t) {
        getLogger().log(Level.WARNING, msg, t);
    }

    public void warn(String msg) {
        getLogger().log(Level.WARNING, msg);
    }

    public void error(String msg, Throwable t) {
        getLogger().log(Level.SEVERE, msg, t);
    }

    public void error(String msg) {
        getLogger().log(Level.SEVERE, msg);
    }

    public void saveResource(String path) {
        saveResource(path, new File(getDataFolder(), path));
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
