package top.mrxiaom.pluginbase.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库信号接收器
 */
public interface IDatabase {
    /**
     * 用户重载数据库时执行的操作，推荐在这里创建数据表
     * @param conn 数据库连接
     * @param tablePrefix 表名前缀
     */
    void reload(Connection conn, String tablePrefix) throws SQLException;
}
