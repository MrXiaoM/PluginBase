package top.mrxiaom.pluginbase.func.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import top.mrxiaom.pluginbase.gui.IGui;

import java.util.Map;

public interface IModel {
    String id();
    String title();
    char[] inventory();
    Map<Character, LoadedIcon> otherIcons();
    default boolean hasPermission(Permissible p) {
        return true;
    }
    ItemStack applyMainIcon(IGui instance, Player player, char id, int index, int appearTimes);
}
