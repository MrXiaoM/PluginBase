package top.mrxiaom.pluginbase.func.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import top.mrxiaom.pluginbase.gui.IGui;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IModel {
    String id();
    String title();
    char[] inventory();
    Map<Character, LoadedIcon> otherIcons();
    default boolean hasPermission(Permissible p) {
        return true;
    }
    default ItemStack applyMainIcon(IGui instance, Player player, char id, int index, int appearTimes) {
        return null;
    }
    default ItemStack applyMainIcon(IGui instance, Player player, char id, int index, int appearTimes, AtomicBoolean ignore) {
        return applyMainIcon(instance, player, id, index, appearTimes);
    }
}
