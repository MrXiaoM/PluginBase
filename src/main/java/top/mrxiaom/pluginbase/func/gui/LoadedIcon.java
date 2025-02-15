package top.mrxiaom.pluginbase.func.gui;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.utils.*;

import java.util.*;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.loadActions;
import static top.mrxiaom.pluginbase.func.gui.IModifier.fit;

public class LoadedIcon {
    private static final List<ITagProvider> tagProviders = new ArrayList<>();
    private final boolean adventure;
    public final String material;
    public final int amount;
    public final String display;
    public final List<String> lore;
    public final boolean glow;
    public final Integer customModelData;
    public final Map<String, String> nbtStrings;
    public final Map<String, String> nbtInts;
    public final List<IAction> leftClickCommands;
    public final List<IAction> rightClickCommands;
    public final List<IAction> shiftLeftClickCommands;
    public final List<IAction> shiftRightClickCommands;
    public final List<IAction> dropCommands;
    public final Object tag;

    LoadedIcon(boolean adventure, String material, int amount, String display, List<String> lore, boolean glow, Integer customModelData, Map<String, String> nbtStrings, Map<String, String> nbtInts, List<IAction> leftClickCommands, List<IAction> rightClickCommands, List<IAction> shiftLeftClickCommands, List<IAction> shiftRightClickCommands, List<IAction> dropCommands, Object tag) {
        this.adventure = adventure;
        this.material = material.toUpperCase();
        this.amount = amount;
        this.display = display;
        this.lore = lore;
        this.glow = glow;
        this.customModelData = customModelData;
        this.nbtStrings = nbtStrings;
        this.nbtInts = nbtInts;
        this.leftClickCommands = leftClickCommands;
        this.rightClickCommands = rightClickCommands;
        this.shiftLeftClickCommands = shiftLeftClickCommands;
        this.shiftRightClickCommands = shiftRightClickCommands;
        this.dropCommands = dropCommands;
        this.tag = tag;
    }

    public ItemStack generateIcon(Player player) {
        return generateIcon(player, null, null);
    }

    public ItemStack generateIcon(Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
        if (material.equals("AIR") || amount == 0) return new ItemStack(Material.AIR);
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(this.material);
        ItemStack item = pair == null ? new ItemStack(Material.PAPER) : ItemStackUtil.legacy(pair);
        item.setAmount(amount);
        if (!display.isEmpty()) {
            String displayName = PAPI.setPlaceholders(player, fit(displayNameModifier, display));
            if (adventure) AdventureItemStack.setItemDisplayName(item, displayName);
            else ItemStackUtil.setItemDisplayName(item, displayName);
        }
        if (!lore.isEmpty()) {
            List<String> loreList = PAPI.setPlaceholders(player, fit(loreModifier, lore));
            if (adventure) AdventureItemStack.setItemLoreMiniMessage(item, loreList);
            else ItemStackUtil.setItemLore(item, loreList);
        }
        if (glow) ItemStackUtil.setGlow(item);
        if (customModelData != null) ItemStackUtil.setCustomModelData(item, customModelData);
        if (!nbtStrings.isEmpty() || !nbtInts.isEmpty()) {
            NBT.modify(item, nbt -> {
                for (Map.Entry<String, String> entry : nbtStrings.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    nbt.setString(entry.getKey(), value);
                }
                for (Map.Entry<String, String> entry : nbtInts.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    Integer i = Util.parseInt(value).orElse(null);
                    if (i == null) continue;
                    nbt.setInteger(entry.getKey(), i);
                }
            });
        }
        return item;
    }

    public void click(Player player, ClickType type) {
        List<IAction> actions;
        switch (type) {
            case LEFT:
                actions = leftClickCommands;
                break;
            case RIGHT:
                actions = rightClickCommands;
                break;
            case SHIFT_LEFT:
                actions = shiftLeftClickCommands;
                break;
            case SHIFT_RIGHT:
                actions = shiftRightClickCommands;
                break;
            case DROP:
                actions = dropCommands;
                break;
            default:
                return;
        }
        List<Pair<String, Object>> args = Lists.newArrayList(
                Pair.of("__internal__loaded_icon", this)
        );
        for (IAction action : actions) {
            action.run(player, args);
        }
    }

    public static LoadedIcon load(ConfigurationSection section, String id) {
        String material, materialStr = section.getString(id + ".material");
        if (materialStr != null) {
            if (section.contains(id + ".data")) { // 兼容旧的选项
                material = materialStr + ":" + section.getInt(id + ".data");
            } else material = "PAPER";
        } else material = "PAPER";

        int amount = section.getInt(id + ".amount", 1);
        String display = section.getString(id + ".display", "");
        List<String> lore = section.getStringList(id + ".lore");
        boolean glow = section.getBoolean(id + ".glow");
        Integer customModelData = section.contains(id + ".custom-model-data") ? section.getInt(id + ".custom-model-data") : null;
        Map<String, String> nbtStrings = new HashMap<>();
        ConfigurationSection section1 = section.getConfigurationSection(id + ".nbt-strings");
        if (section1 != null) for (String key : section1.getKeys(false)) {
            nbtStrings.put(key, section1.getString(key, ""));
        }
        Map<String, String> nbtInts = new HashMap<>();
        section1 = section.getConfigurationSection(id + ".nbt-ints");
        if (section1 != null) for (String key : section1.getKeys(false)) {
            nbtInts.put(key, section1.getString(key, ""));
        }
        List<IAction> leftClickCommands = loadActions(section, id + ".left-click-commands");
        List<IAction> rightClickCommands = loadActions(section, id + ".right-click-commands");
        List<IAction> shiftLeftClickCommands = loadActions(section, id + ".shift-left-click-commands");
        List<IAction> shiftRightClickCommands = loadActions(section, id + ".shift-left-click-commands");
        List<IAction> dropCommands = loadActions(section, id + ".drop-commands");
        Object tag = null;
        for (ITagProvider provider : tagProviders) {
            if ((tag = provider.provide(section, id)) != null) {
                break;
            }
        }
        return new LoadedIcon(BukkitPlugin.getInstance().options.adventure(), material, amount, display, lore, glow, customModelData, nbtStrings, nbtInts, leftClickCommands, rightClickCommands, shiftLeftClickCommands, shiftRightClickCommands, dropCommands, tag);
    }

    public static void registerTagProvider(ITagProvider provider) {
        tagProviders.add(provider);
        tagProviders.sort(Comparator.comparingInt(ITagProvider::priority));
    }
}
