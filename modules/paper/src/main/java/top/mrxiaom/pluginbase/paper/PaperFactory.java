package top.mrxiaom.pluginbase.paper;

import top.mrxiaom.pluginbase.utils.inventory.BukkitInventoryFactory;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.paper.inventory.PaperInventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.item.LegacyItemEditor;
import top.mrxiaom.pluginbase.paper.item.PaperItemEditor;

public class PaperFactory {
    public static ItemEditor createItemEditor() {
        try {
            return new PaperItemEditor();
        } catch (Throwable t) {
            return new LegacyItemEditor();
        }
    }

    public static InventoryFactory createInventoryFactory() {
        try {
            if (PaperInventoryFactory.test()) {
                return new PaperInventoryFactory();
            }
        } catch (Throwable ignored) {
        }
        return new BukkitInventoryFactory();
    }
}
