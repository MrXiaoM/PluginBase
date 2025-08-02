package top.mrxiaom.pluginbase.utils.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ItemEditor {
    ComponentSerializer<Component, ?, String> serializer();
    @Nullable Component getItemDisplayName(ItemStack item);
    void setItemDisplayName(ItemStack item, @Nullable Component displayName);
    @NotNull List<Component> getItemLore(ItemStack item);
    void setItemLore(ItemStack item, @Nullable List<Component> lore);
}
