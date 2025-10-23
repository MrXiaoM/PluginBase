package top.mrxiaom.pluginbase;

import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.inventory.BukkitInventoryFactory;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.item.LegacyItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.BukkitScheduler;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.*;

/**
 * PluginBase 抽象插件主类，所有功能的核心，包含一些自定义配置与常用工具
 */
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

    /**
     * 获取插件主类实例
     */
    public static BukkitPlugin getInstance() {
        return instance;
    }
    private final List<Class<? extends AbstractPluginHolder<?>>> modulesToRegister = new ArrayList<>();
    private boolean pluginEnabled = false;
    /**
     * 获取插件主类配置参数
     */
    public final Options options;
    /**
     * 已包装的插件 ClassLoader，用于快捷添加 URL 到插件的 URLClassLoader 中
     */
    protected final ClassLoaderWrapper classLoader;
    /**
     * 已包装的调度器，用于兼容 Folia 服务端
     */
    protected IScheduler scheduler = new BukkitScheduler(this);
    /**
     * 物品栏创建工厂
     */
    protected InventoryFactory inventory;
    private FileConfiguration config;
    public BukkitPlugin(OptionsBuilder builder) {
        this(builder.build());
    }
    private BukkitPlugin(Options options) {
        if (className.equals("group.pluginbase.BukkitPlugin".replace("group", "top.mrxiaom"))) {
            throw new IllegalStateException("PluginBase 依赖没有 relocate 到插件包，插件无法正常工作，请联系开发者解决该问题\n参考文档: https://plugins.mcio.dev/elopers/base/buildscript");
        }
        instance = this;
        this.options = options;
        this.classLoader = initClassLoader((URLClassLoader) getClassLoader());
        if (this.options.libraries() || this.options.database) {
            loadLibraries();
        }
    }

    /**
     * 初始化类加载器包装实现
     * @param classLoader 旧的类加载器
     */
    @NotNull
    protected ClassLoaderWrapper initClassLoader(URLClassLoader classLoader) {
        return ClassLoaderWrapper.resolve(classLoader);
    }

    /**
     * 初始化自定义经济实现，设置选项 <code>economy(EnumEconomy.CUSTOM)</code> 时使用
     * @see BukkitPlugin#warnNoEconomy()
     */
    @Nullable
    protected IEconomy initCustomEconomy() {
        return null;
    }

    /**
     * 初始化物品编辑器。开发者不应该直接使用本方法
     * @see AdventureItemStack#getItemEditor()
     */
    @NotNull
    public ItemEditor initItemEditor() {
        return new LegacyItemEditor();
    }

    /**
     * 初始化物品栏创建工厂。开发者不应该直接使用本方法
     * @see BukkitPlugin#createInventory(InventoryHolder, int, String)
     */
    @NotNull
    public InventoryFactory initInventoryFactory() {
        return new BukkitInventoryFactory();
    }

    protected void warnNoEconomy() {
        warn("未发现经济接口实现，插件将卸载");
    }

    protected File getLibrariesFolder() {
        return new File(getDataFolder(), "libraries");
    }

    protected void loadLibraries() {
        File librariesFolder = getLibrariesFolder();
        if (!librariesFolder.exists()) {
            createLibrariesFolder(librariesFolder);
        }
        List<File> files = listLibraries(librariesFolder);
        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith(".jar")) continue;
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

    /**
     * 获取已包装的调度器
     */
    @NotNull
    public IScheduler getScheduler() {
        return scheduler;
    }

    /**
     * 创建一个物品栏实例
     * @param holder 物品栏持有数据
     * @param size 大小 (9的倍数)
     * @param title 标题，支持 MiniMessage
     */
    @NotNull
    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        return inventory.create(holder, size, title);
    }

    /**
     * 在开启 database 选项时，从数据库连接池获取一个连接
     */
    public Connection getConnection() {
        if (options.databaseHolder == null) {
            throw new UnsupportedOperationException("请在插件主类配置添加 database(true)");
        }
        return options.databaseHolder.getConnection();
    }

    protected void beforeLoad() {

    }

    protected void afterLoad() {

    }

    /**
     * 在插件启用的早期阶段执行的操作
     * @return <code>true</code> 代表允许启用，<code>false</code> 代表遇到问题，例如未安装依赖插件，将会卸载插件
     */
    protected boolean beforeEnableEarly() {
        return true;
    }

    /**
     * 在插件启用时，早期阶段之后，模块加载之前执行的操作
     */
    protected void beforeEnable() {

    }

    /**
     * 在插件启用时，模块加载之后，重载配置之前执行的操作
     */
    protected void afterEnableModules() {

    }

    /**
     * 在插件启用时，重载配置之后的最后阶段执行的操作
     */
    protected void afterEnable() {

    }

    /**
     * 在插件卸载时，最先执行的操作
     */
    protected void beforeDisable() {

    }

    /**
     * 在插件卸载时，模块全部卸载完成后执行的操作
     */
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

    /**
     * 获取插件功能模块的构造函数参数中，插件主类的类型
     */
    public Class<?> getConstructorType() {
        return getClass();
    }

    @Override
    @Deprecated
    @SuppressWarnings({"unchecked"})
    public void onEnable() {
        Util.init(this);
        inventory = initInventoryFactory();
        if (options.enable(this)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!beforeEnableEarly()) {
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

        afterEnableModules();

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

    /**
     * 在配置有设置 <code>goto</code> 时，转而去读取其指向的配置文件<br>
     * 以免出现死循环，最多支持转跳 <code>64</code> 次。
     */
    public FileConfiguration resolveGotoFlag(FileConfiguration config) {
        return resolveGotoFlag(config, 0);
    }

    private FileConfiguration resolveGotoFlag(FileConfiguration last, int times) {
        if (times > 64) {
            warn("配置文件中的 goto 跳转次数过多，请自行检查 goto 标签是否有循环调用问题");
            return last;
        }
        String gotoFlag = last.getString("goto", null);
        if (gotoFlag != null) {
            File file = resolve(gotoFlag);
            YamlConfiguration newConfig = Util.load(file);
            return resolveGotoFlag(newConfig, times + 1);
        }
        return last;
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        if (config == null) {
            this.reloadConfig();
        }
        return config;
    }

    @Override
    public void reloadConfig() {
        FileConfiguration config;
        if (!options.disableDefaultConfig) {
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                saveResource("config.yml", file);
            }
            this.config = config = Util.load(file);
            if (options.enableConfigGotoFlag) {
                config = resolveGotoFlag(config);
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

    /**
     * 保存插件资源到插件数据目录下，若文件已存在，将覆盖该文件
     * @param path 资源路径
     */
    public void saveResource(String path) {
        saveResource(path, new File(getDataFolder(), path));
    }

    /**
     * 保存插件资源到指定文件
     * @param path 资源路径
     * @param file 文件
     */
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

    /**
     * 根据输入字符串获取文件实例
     * <ul>
     *     <li>以 <code>./</code> 开头时，路径相对于 <code>服务端/plugins/插件名/</code></li>
     *     <li>其他情况下，路径相对于 <code>服务端/</code> 或者是绝对路径</li>
     * </ul>
     */
    public File resolve(String path) {
        return path.startsWith("./") ? new File(getDataFolder(), path.substring(2)) : new File(path);
    }
}
