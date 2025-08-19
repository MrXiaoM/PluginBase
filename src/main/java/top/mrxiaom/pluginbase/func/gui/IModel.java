package top.mrxiaom.pluginbase.func.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import top.mrxiaom.pluginbase.gui.IGuiHolder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 菜单配置模型
 */
public interface IModel {
    /**
     * 模型 ID，通常为文件名
     */
    String id();

    /**
     * 菜单标题
     */
    String title();

    /**
     * 菜单界面布局
     */
    char[] inventory();

    /**
     * 菜单额外图标列表
     */
    Map<Character, LoadedIcon> otherIcons();

    /**
     * 获取玩家是否有权限打开这个菜单
     * @param p 玩家
     */
    default boolean hasPermission(Permissible p) {
        return true;
    }

    /**
     * @see IModel#applyMainIcon(IGuiHolder, Player, char, int, int, AtomicBoolean)
     */
    default ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes) {
        return null;
    }

    /**
     * 获取菜单中的主要图标
     * @param instance 菜单实例
     * @param player 查看这个菜单的玩家
     * @param id 图标ID
     * @param index 图标索引 (位置)
     * @param appearTimes 该图标ID已出现的次数
     * @param ignore 是否忽略设置这个图标
     * @return 图标物品
     */
    default ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes, AtomicBoolean ignore) {
        return applyMainIcon(instance, player, id, index, appearTimes);
    }

    /**
     * 获取菜单中的额外图标
     * @param instance 菜单实例
     * @param player 查看这个菜单的玩家
     * @param id 图标ID
     * @param index 图标索引 (位置)
     * @param appearTimes 该图标ID已出现的次数
     * @param icon 额外图标配置
     * @return 图标物品
     */
    default ItemStack applyOtherIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes, LoadedIcon icon) {
        return icon.generateIcon(player);
    }
}
