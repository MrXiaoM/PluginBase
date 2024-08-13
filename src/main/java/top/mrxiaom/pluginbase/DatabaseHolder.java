package top.mrxiaom.pluginbase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.utils.Util;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.Util.stackTraceToString;

public class DatabaseHolder {
    BukkitPlugin plugin;
    HikariConfig hikariConfig;
    HikariDataSource dataSource = null;
    List<IDatabase> databases = new ArrayList<>();
    private boolean firstConnectFlag = false;
    protected DatabaseHolder(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerDatabase(IDatabase... databases) {
        this.databases.addAll(Arrays.asList(databases));
    }

    @Nullable
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Throwable t) {
            plugin.getLogger().warning(stackTraceToString(t));
            return null;
        }
    }

    public void reloadConfig() {
        File file = new File(plugin.getDataFolder(), "database.yml");
        if (!file.exists()) {
            plugin.saveResource("database.yml", true);
        }
        reloadFromFile(file);
    }
    private void reloadFromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("goto")) {
            File gotoFile = new File(config.getString("goto", ""));
            if (gotoFile.exists() && gotoFile.isFile()) {
                reloadFromFile(gotoFile);
                return;
            }
        }
        String type = config.getString("type", "sqlite").toLowerCase();
        String driver;
        switch (type) {
            case "mysql":
                driver = checkDriver("MySQL", "com.mysql.cj.jdbc.Driver");
                break;
            case "sqlite":
            default:
                driver = checkDriver("SQLite", "org.sqlite.JDBC");
                break;
        }
        if (driver == null) return;
        String query = config.getString("query", "");
        query = (query.isEmpty() ? "" : ("?" + query));
        hikariConfig = new HikariConfig();
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaxLifetime(120000L);
        hikariConfig.setIdleTimeout(5000L);
        hikariConfig.setConnectionTimeout(5000L);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaximumPoolSize(100);
        if (driver.equals("com.mysql.cj.jdbc.Driver")) {
            String host = config.getString("mysql.host", "localhost");
            int port = config.getInt("mysql.port", 3306);
            String user = config.getString("mysql.user", "root");
            String pass = config.getString("mysql.pass", "root");
            String database = config.getString("mysql.database", "db");
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + query);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
        }
        if (driver.equals("org.sqlite.JDBC")) {
            String database = config.getString("sqlite.file", "database.db");
            hikariConfig.setJdbcUrl("jdbc:sqlite:plugins/" + plugin.getName() + "/" + database);
        }
        if (!firstConnectFlag && !plugin.options.reconnectDatabaseWhenReloadConfig) {
            reconnect();
        }
    }

    private String checkDriver(String type, String driver) {
        if (!Util.isPresent(driver)) {
            plugin.warn("预料中的错误: 未找到 " + type + " JDBC Driver: " + driver);
            plugin.warn("正在卸载插件，请使用最新版 Spigot 或其衍生服务端");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return null;
        }
        return driver;
    }

    public void reconnect() {
        if (hikariConfig == null) {
            plugin.getLogger().warning("未找到数据库配置，停止连接");
            return;
        }
        firstConnectFlag = true;
        if (dataSource != null) dataSource.close();
        dataSource = new HikariDataSource(hikariConfig);
        plugin.getLogger().info("正在连接数据库...");
        Connection conn = getConnection();
        if (conn == null) plugin.getLogger().warning("无法连接到数据库!");
        else {
            for (IDatabase db : databases) db.reload(conn);
            plugin.getLogger().info("数据库连接成功");
            try {
                conn.close();
            } catch (Throwable t) {
                plugin.warn("连接数据库时出现一个错误", t);
            }
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
