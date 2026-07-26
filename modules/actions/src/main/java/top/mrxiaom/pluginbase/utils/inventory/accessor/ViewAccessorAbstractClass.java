package top.mrxiaom.pluginbase.utils.inventory.accessor;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.InventoryViewAccessor;

import java.lang.reflect.Modifier;

public class ViewAccessorAbstractClass implements InventoryViewAccessor {
    public static final Provider PROVIDER = new Provider() {
        @Override
        public InventoryViewAccessor get(HumanEntity entity) {
            return new ViewAccessorAbstractClass(entity.getOpenInventory());
        }

        @Override
        public InventoryViewAccessor get(InventoryEvent event) {
            return new ViewAccessorAbstractClass(event.getView());
        }
    };
    private final InventoryView view;
    public ViewAccessorAbstractClass(InventoryView view) {
        this.view = view;
    }

    public static Provider testBestProvider() {
        if (Modifier.isAbstract(InventoryView.class.getModifiers())) {
            return ViewAccessorAbstractClass.PROVIDER;
        } else {
            return ViewAccessorInterface.PROVIDER;
        }
    }

    @Override
    public @NotNull Object getHandle() {
        return view;
    }

    @Override
    public @NotNull Inventory getTopInventory() {
        return view.getTopInventory();
    }

    @Override
    public @NotNull Inventory getBottomInventory() {
        return view.getBottomInventory();
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return view.getPlayer();
    }

    @Override
    public @NotNull InventoryType getType() {
        return view.getType();
    }

    @Override
    public @Nullable ItemStack getItem(int slot) {
        return view.getItem(slot);
    }

    @Override
    public void setItem(int slot, @Nullable ItemStack item) {
        view.setItem(slot, item);
    }

    @Override
    public void setCursor(@Nullable ItemStack item) {
        view.setCursor(item);
    }

    @Override
    public @Nullable ItemStack getCursor() {
        return view.getCursor();
    }

    @Override
    public void close() {
        view.close();
    }

    @Override
    public @NotNull String getTitle() {
        return view.getTitle();
    }

    @Override
    public void openInventory(HumanEntity entity) {
        entity.openInventory(view);
    }
}
