package top.mrxiaom.pluginbase.economy;

import org.bukkit.OfflinePlayer;

/**
 * 无经济插件，所有玩家的余额皆为 0，任何操作均为允许
 */
public class NoEconomy implements IEconomy {
    public static NoEconomy INSTANCE = new NoEconomy();
    private NoEconomy() {}
    @Override
    public String getName() {
        return "none";
    }

    @Override
    public double get(OfflinePlayer player) {
        return 0;
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return true;
    }

    @Override
    public void giveMoney(OfflinePlayer player, double money) {
    }

    @Override
    public void takeMoney(OfflinePlayer player, double money) {
    }
}
