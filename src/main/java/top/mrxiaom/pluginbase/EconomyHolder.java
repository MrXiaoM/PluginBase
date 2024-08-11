package top.mrxiaom.pluginbase;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHolder {
    BukkitPlugin plugin;
    Economy economy;
    protected EconomyHolder(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    public double get(OfflinePlayer player) {
        if (economy == null) return 0;
        return economy.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double money) {
        if (economy == null) return false;
        return economy.has(player, money);
    }

    public void giveMoney(OfflinePlayer player, double money) {
        if (economy == null) return;
        economy.depositPlayer(player, money);
    }

    public void takeMoney(OfflinePlayer player, double money) {
        if (economy == null) return;
        economy.withdrawPlayer(player, money);
    }
}
