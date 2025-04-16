package top.mrxiaom.pluginbase.economy;

import org.bukkit.OfflinePlayer;

public interface IEconomy {
    String getName();
    double get(OfflinePlayer player);
    boolean has(OfflinePlayer player, double money);
    void giveMoney(OfflinePlayer player, double money);
    void takeMoney(OfflinePlayer player, double money);
}
