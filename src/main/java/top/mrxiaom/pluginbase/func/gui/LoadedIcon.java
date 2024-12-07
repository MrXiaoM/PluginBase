package top.mrxiaom.pluginbase.func.gui;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadedIcon {
    private final boolean adventure;
    public final Material material;
    public final int data;
    public final int amount;
    public final String display;
    public final List<String> lore;
    public final boolean glow;
    public final Integer customModelData;
    public final Map<String, String> nbtStrings;
    public final List<String> leftClickCommands;
    public final List<String> rightClickCommands;
    public final List<String> shiftLeftClickCommands;
    public final List<String> shiftRightClickCommands;
    public final List<String> dropCommands;

    LoadedIcon(boolean adventure, Material material, int data, int amount, String display, List<String> lore, boolean glow, Integer customModelData, Map<String, String> nbtStrings, List<String> leftClickCommands, List<String> rightClickCommands, List<String> shiftLeftClickCommands, List<String> shiftRightClickCommands, List<String> dropCommands) {
        this.adventure = adventure;
        this.material = material;
        this.data = data;
        this.amount = amount;
        this.display = display;
        this.lore = lore;
        this.glow = glow;
        this.customModelData = customModelData;
        this.nbtStrings = nbtStrings;
        this.leftClickCommands = leftClickCommands;
        this.rightClickCommands = rightClickCommands;
        this.shiftLeftClickCommands = shiftLeftClickCommands;
        this.shiftRightClickCommands = shiftRightClickCommands;
        this.dropCommands = dropCommands;
    }

    @SuppressWarnings({"deprecation"})
    public ItemStack generateIcon(Player player) {
        ItemStack item = data == 0 ? new ItemStack(material, amount) : new ItemStack(material, amount, (short) data);
        if (!display.isEmpty()) {
            String displayName = PAPI.setPlaceholders(player, display);
            if (adventure) AdventureItemStack.setItemDisplayName(item, displayName);
            else ItemStackUtil.setItemDisplayName(item, displayName);
        }
        if (!lore.isEmpty()) {
            List<String> loreList = PAPI.setPlaceholders(player, lore);
            if (adventure) AdventureItemStack.setItemLore(item, loreList);
            else ItemStackUtil.setItemLore(item, loreList);
        }
        if (glow) ItemStackUtil.setGlow(item);
        if (customModelData != null) ItemStackUtil.setCustomModelData(item, customModelData);
        if (!nbtStrings.isEmpty()) {
            NBT.modify(item, nbt -> {
                for (Map.Entry<String, String> entry : nbtStrings.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    nbt.setString(entry.getKey(), value);
                }
            });
        }
        return item;
    }

    public void click(Player player, ClickType type) {
        List<String> commands;
        switch (type) {
            case LEFT:
                commands = leftClickCommands;
                break;
            case RIGHT:
                commands = rightClickCommands;
                break;
            case SHIFT_LEFT:
                commands = shiftLeftClickCommands;
                break;
            case SHIFT_RIGHT:
                commands = shiftRightClickCommands;
                break;
            case DROP:
                commands = dropCommands;
                break;
            default:
                return;
        }
        Util.runCommands(player, commands);
    }

    public static LoadedIcon load(ConfigurationSection section, String id) {
        Material material = Util.valueOr(Material.class, section.getString(id + ".material"), Material.PAPER);
        int amount = section.getInt(id + ".amount", 1);
        int data = section.getInt(id + ".data");
        String display = section.getString(id + ".display", "");
        List<String> lore = section.getStringList(id + ".lore");
        boolean glow = section.getBoolean(id + ".glow");
        Integer customModelData = section.contains(id + ".custom-model-data") ? section.getInt(id + ".custom-model-data") : null;
        Map<String, String> nbtStrings = new HashMap<>();
        ConfigurationSection section1 = section.getConfigurationSection(id + ".nbt-strings");
        if (section1 != null) for (String key : section1.getKeys(false)) {
            nbtStrings.put(key, section1.getString(key, ""));
        }
        List<String> leftClickCommands = section.getStringList(id + "left-click-commands");
        List<String> rightClickCommands = section.getStringList(id + "right-click-commands");
        List<String> shiftLeftClickCommands = section.getStringList(id + "shift-left-click-commands");
        List<String> shiftRightClickCommands = section.getStringList(id + "shift-left-click-commands");
        List<String> dropCommands = section.getStringList(id + "drop-commands");
        return new LoadedIcon(BukkitPlugin.getInstance().options.adventure(), material, data, amount, display, lore, glow, customModelData, nbtStrings, leftClickCommands, rightClickCommands, shiftLeftClickCommands, shiftRightClickCommands, dropCommands);
    }
}
