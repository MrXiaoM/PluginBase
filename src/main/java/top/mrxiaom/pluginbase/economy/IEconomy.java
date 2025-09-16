package top.mrxiaom.pluginbase.economy;

import org.bukkit.OfflinePlayer;

/**
 * 经济插件抽象接口
 */
public interface IEconomy {
    /**
     * 插件展示名
     */
    String getName();

    /**
     * 获取玩家货币数量
     * @param player 玩家
     */
    double get(OfflinePlayer player);

    /**
     * 获取玩家是否有足够的货币
     * @param player 玩家
     * @param money 货币数量
     */
    boolean has(OfflinePlayer player, double money);

    /**
     * 给予玩家货币
     * @param player 玩家
     * @param money 货币数量
     * @return 是否操作成功
     */
    boolean giveMoney(OfflinePlayer player, double money);

    /**
     * 从玩家那里拿走货币
     * @param player 玩家
     * @param money 货币数量
     * @return 是否操作成功
     */
    boolean takeMoney(OfflinePlayer player, double money);
}
