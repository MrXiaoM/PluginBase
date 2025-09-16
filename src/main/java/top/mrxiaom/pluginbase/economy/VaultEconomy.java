package top.mrxiaom.pluginbase.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

/**
 * Vault 经济插件
 */
public class VaultEconomy implements IEconomy {
    private final Economy economy;
    private final String name;

    public VaultEconomy(Economy economy) {
        this.economy = economy;
        this.name = "Vault{" + economy.getName() + "}";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double get(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return economy.has(player, money);
    }

    @Override
    public boolean giveMoney(OfflinePlayer player, double money) {
        return economy.depositPlayer(player, money).transactionSuccess();
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        if (has(player, money)) {
            return economy.withdrawPlayer(player, money).transactionSuccess();
        }
        return false;
    }
}
