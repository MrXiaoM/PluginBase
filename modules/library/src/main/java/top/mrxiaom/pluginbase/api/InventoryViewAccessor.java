package top.mrxiaom.pluginbase.api;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InventoryViewAccessor {
    @NotNull Object getHandle();
    @NotNull
    Inventory getTopInventory();
    @NotNull
    Inventory getBottomInventory();
    @NotNull
    HumanEntity getPlayer();
    @NotNull
    InventoryType getType();
    @Nullable
    ItemStack getItem(int slot);
    void setItem(int slot, @Nullable ItemStack item);
    void setCursor(@Nullable ItemStack item);
    @Nullable
    ItemStack getCursor();
    void close();
    @NotNull
    String getTitle();

    void openInventory(HumanEntity entity);

    interface Provider {
        InventoryViewAccessor get(HumanEntity entity);
        InventoryViewAccessor get(InventoryEvent event);
    }
}
