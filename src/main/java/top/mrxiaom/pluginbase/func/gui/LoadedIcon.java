package top.mrxiaom.pluginbase.func.gui;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.utils.*;

import java.util.*;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.loadActions;

public class LoadedIcon {
    private static final List<ITagProvider> tagProviders = new ArrayList<>();
    private final boolean adventure;
    public final Material material;
    public final int data;
    public final int amount;
    public final String display;
    public final List<String> lore;
    public final boolean glow;
    public final Integer customModelData;
    public final Map<String, String> nbtStrings;
    public final List<IAction> leftClickCommands;
    public final List<IAction> rightClickCommands;
    public final List<IAction> shiftLeftClickCommands;
    public final List<IAction> shiftRightClickCommands;
    public final List<IAction> dropCommands;
    public final Object tag;

    LoadedIcon(boolean adventure, Material material, int data, int amount, String display, List<String> lore, boolean glow, Integer customModelData, Map<String, String> nbtStrings, List<IAction> leftClickCommands, List<IAction> rightClickCommands, List<IAction> shiftLeftClickCommands, List<IAction> shiftRightClickCommands, List<IAction> dropCommands, Object tag) {
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
        this.tag = tag;
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
        Pair<String, Object>[] args = Pair.array(0);
        for (IAction action : actions) {
            action.run(player, args);
        }
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
        List<IAction> leftClickCommands = loadActions(section, id + "left-click-commands");
        List<IAction> rightClickCommands = loadActions(section, id + "right-click-commands");
        List<IAction> shiftLeftClickCommands = loadActions(section, id + "shift-left-click-commands");
        List<IAction> shiftRightClickCommands = loadActions(section, id + "shift-left-click-commands");
        List<IAction> dropCommands = loadActions(section, id + "drop-commands");
        Object tag = null;
        for (ITagProvider provider : tagProviders) {
            if ((tag = provider.provide(section, id)) != null) {
                break;
            }
        }
        return new LoadedIcon(BukkitPlugin.getInstance().options.adventure(), material, data, amount, display, lore, glow, customModelData, nbtStrings, leftClickCommands, rightClickCommands, shiftLeftClickCommands, shiftRightClickCommands, dropCommands, tag);
    }

    public static void registerTagProvider(ITagProvider provider) {
        tagProviders.add(provider);
        tagProviders.sort(Comparator.comparingInt(ITagProvider::priority));
    }
}
