# 数据库连接池 HikariCP

要开启数据库支持，先到构建脚本添加依赖
```kotlin
// ...
dependencies {
    // ...
    implementation("com.zaxxer:HikariCP:4.0.3")
}
// 通过 shadow 打进 jar 里
// 看个人喜好，在 plugin.yml 声明下载依赖也行
tasks {
    shadowJar {
        // ...
        mapOf(
            // ...
            "com.zaxxer.hikari" to "hikari",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
// ...
```
MySQL 和 SQLite 的 JDBC Driver 已经打到 spigot 核心里面了，所以不需要在插件里带上 JDBC Driver。

然后将 [database.yml](https://github.com/MrXiaoM/PluginBase/blob/main/configurations/database.yml) 放到 `/src/main/resources`，再到主类启用数据库支持
```java
public class PluginMain extends BukkitPlugin {
    // 1. 在插件主类构造函数启用数据库支持
    public PluginMain() {
        super(options() // ...
                .database(true)
                // 是否在 reloadConfig 时重连数据库
                // 避免频繁断连重连，更推荐分离 重载配置文件 和 重连数据库
                .reconnectDatabaseWhenReloadConfig(false)
                // ...
        );
    }
    
    // 2. 在 beforeEnable 或之前注册数据库调用器实例
    //    如何创建数据库调用器 (IDatabase) 将会在下方说明
    //
    //    像这样放在主类比较容易调用，无论在哪里都有 plugin 实例可以使用
    //    想把它注册成模块也行，看个人喜好
    public final ExampleDatabase exampleDatabase = new ExampleDatabase(this);
    @Override
    protected void beforeEnable() {
        options.registerDatabase(exampleDatabase);

        // 3. 如需重新连接数据库，可用 options.database().reconnect();
        // 4. 如需从线程池拉取数据库连接，可用 this.getConnection();
    }
}
```

# 创建数据库调用器

```java
package top.mrxiaom.pluginbase.database;

// 1. 实现 IDatabase，其它随意
public class ExampleDatabase extends AbstractPluginHolder implements IDatabase {
    String tableName;
    // 2. 确保能取到主类示例
    public ExampleDatabase(PluginMain plugin) {
        super(plugin);
    }

    // 3. 在 reload 时建表
    @Override
    public void reload(Connection conn, String tablePrefix) {
        tableName = (tablePrefix + "example").toUpperCase();
        try (PreparedStatement statement = conn.prepareStatement(
                "CREATE TABLE " + tableName + " IF NOT EXISTS example(`foo` int, `bar` int);"
        )) {
            statement.execute();
        } catch (SQLException e) {
            warn(e);
        }
    }

    // 4. 执行你想要的其它操作
    //    plugin.getConnection() 可以从连接池申请一个连接
    //    用 try-with-resource 用法，用完连接后自动 close 归还到连接池
    public void addEntry(int foo, int bar) {
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO " + tableName + " (`foo`, `bar`) VALUES (?, ?);"
            )) {
                statement.setInt(1, foo);
                statement.setInt(2, bar);
                statement.execute();
            }
        } catch (SQLException e) {
            warn(e);
        }
    }
}
```
