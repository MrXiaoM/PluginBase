package top.mrxiaom.pluginbase.utils.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ItemEditor {
    /**
     * 获取 Component 序列化器
     */
    ComponentSerializer<Component, ?, String> serializer();

    /**
     * 获取物品名
     * @param item 物品
     * @return 在找不到物品名时，返回 <code>null</code>
     */
    @Nullable Component getItemDisplayName(ItemStack item);

    /**
     * 设置物品名
     * @param item 物品
     * @param displayName 在传入 <code>null</code> 时，删除物品名
     */
    void setItemDisplayName(ItemStack item, @Nullable Component displayName);

    /**
     * 获取物品 Lore
     * @param item 物品
     * @return 在找不到物品 Lore 时，返回空列表
     */
    @NotNull List<Component> getItemLore(ItemStack item);

    /**
     * 设置物品 Lore
     * @param item 物品
     * @param lore 在传入 <code>null</code> 时，删除物品 Lore
     */
    void setItemLore(ItemStack item, @Nullable List<Component> lore);
}
