package top.mrxiaom.pluginbase.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabase {
    void reload(Connection conn, String tablePrefix) throws SQLException;
}
