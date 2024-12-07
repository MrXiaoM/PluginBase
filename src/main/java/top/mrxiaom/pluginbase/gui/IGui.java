package top.mrxiaom.pluginbase.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.BukkitPlugin;

public interface IGui {
    /**
     * 获取正在预览界面的玩家
     */
    Player getPlayer();

    /**
     * 创建物品栏界面
     * @return 物品栏
     */
    Inventory newInventory();

    /**
     * 界面物品点击时执行
     * @param action 玩家进行的物品栏操作
     * @param click 玩家进行的点击操作
     * @param slotType 格子类型
     * @param slot 格子索引
     * @param currentItem 点击的物品
     * @param cursor 点击时指针持有的物品
     * @param view 物品栏界面
     * @param event 点击事件
     */
    void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event);

    /**
     * 界面物品拖拽时执行
     * @param view 物品栏界面
     * @param event 拖拽事件
     */
    default void onDrag(InventoryView view, InventoryDragEvent event) {
        event.setCancelled(true);
    }

    /**
     * 界面关闭时执行
     * @param view 物品栏界面
     */
    default void onClose(InventoryView view) {

    }

    /**
     * 为玩家打开或重新打开界面
     */
    default void open() {
        BukkitPlugin.getInstance().getGuiManager().openGui(this);
    }
}
