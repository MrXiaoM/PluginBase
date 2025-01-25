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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private String tablePrefix;
    private String driver;
    private String version = "unknown";
    protected DatabaseHolder(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerDatabase(IDatabase... databases) {
        this.databases.addAll(Arrays.asList(databases));
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public String getDriver() {
        return driver;
    }

    public boolean isSQLite() {
        return "org.sqlite.JDBC".equals(getDriver());
    }

    public boolean isMySQL() {
        return "com.mysql.cj.jdbc.Driver".equals(getDriver());
    }

    public String getVersion() {
        return version;
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
        tablePrefix = config.getString("table_prefix", "");
        String type = config.getString("type", "sqlite").toLowerCase();
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
        hikariConfig.setMaxLifetime(config.getLong("hikari.max_lifetime", 120000L));
        hikariConfig.setConnectionTimeout(config.getLong("hikari.connection_timeout", 5000L));
        hikariConfig.setDriverClassName(driver);
        if (type.equals("sqlite")) {
            hikariConfig.setMinimumIdle(1);
            hikariConfig.setMaximumPoolSize(1);
        } else {
            hikariConfig.setIdleTimeout(config.getLong("hikari.idle_timeout", 10000L));
            hikariConfig.setMinimumIdle(config.getInt("hikari.minimum_idle", 8));
            hikariConfig.setMaximumPoolSize(config.getInt("hikari.maximum_pool_size", 36));
        }
        if (isMySQL()) {
            String host = config.getString("mysql.host", "localhost");
            int port = config.getInt("mysql.port", 3306);
            String user = config.getString("mysql.user", "root");
            String pass = config.getString("mysql.pass", "root");
            String database = config.getString("mysql.database", "db");
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + query);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            hikariConfig.setConnectionTestQuery("SELECT NOW();");
        }
        if (isSQLite()) {
            String database = config.getString("sqlite.file", "database.db");
            hikariConfig.setJdbcUrl("jdbc:sqlite:plugins/" + plugin.getName() + "/" + database);
            hikariConfig.setConnectionTestQuery("SELECT CURRENT_TIMESTAMP;");
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
        version = "unknown";
        Connection conn = getConnection();
        if (conn == null) plugin.getLogger().warning("无法连接到数据库!");
        else {
            try (Connection connection = conn) {
                try (PreparedStatement ps = connection.prepareStatement(isSQLite()
                        ? "SELECT SQLITE_VERSION();"
                        : "SELECT VERSION();");
                     ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        version = result.getString(1);
                    }
                } catch (SQLException e) {
                    plugin.warn("获取目标数据库版本时出现一个错误: " + e);
                }
                for (IDatabase db : databases) {
                    db.reload(connection, getTablePrefix());
                }
                plugin.getLogger().info("数据库连接成功");
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
