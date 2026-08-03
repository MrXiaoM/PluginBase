package top.mrxiaom.pluginbase.paper.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;

public class PaperInventoryFactory implements InventoryFactory {
    public PaperInventoryFactory() {}

    @Override
    @SuppressWarnings("deprecation")
    public Inventory create(InventoryHolder owner, int size, String title) {
        try {
            Component parsed = AdventureUtil.miniMessage(title);
            return Bukkit.createInventory(owner, size, parsed);
        } catch (LinkageError e) { // 1.16 以下的旧版本 Paper 服务端不支持这个接口
            Component parsed = AdventureUtil.miniMessage(title);
            return Bukkit.createInventory(owner, size, AdventureUtil.legacySection(parsed));
        }
    }

    public static boolean test() {
        try {
            Bukkit.class.getDeclaredMethod("createInventory", InventoryHolder.class, InventoryType.class, Component.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
