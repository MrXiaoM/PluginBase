package top.mrxiaom.pluginbase.database;

import java.sql.Connection;

public interface IDatabase {
    void reload(Connection conn);
}
