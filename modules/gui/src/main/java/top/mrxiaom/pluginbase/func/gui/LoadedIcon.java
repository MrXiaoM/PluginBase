package top.mrxiaom.pluginbase.func.gui;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.*;
import top.mrxiaom.pluginbase.utils.depend.PAPI;

import java.util.*;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;
import static top.mrxiaom.pluginbase.func.gui.IModifier.fit;

/**
 * 通用界面图标配置
 */
public class LoadedIcon {
    private static final List<ITagProvider> tagProviders = new ArrayList<>();
    private final boolean adventure = BukkitPlugin.getInstance().options.adventure();
    /**
     * 物品材质
     */
    public final String material;
    /**
     * 物品数量
     */
    public final int amount;
    /**
     * 物品显示名称
     */
    public final String display;
    /**
     * 物品 Lore
     */
    public final List<String> lore;
    /**
     * 物品是否发光，即物品是否添加一个附魔并隐藏，以产生附魔光泽
     */
    public final boolean glow;
    /**
     * 物品的自定义模型标记
     */
    public final Integer customModelData;
    /**
     * 物品的额外 NBT
     */
    public final Map<String, String> nbtStrings;
    /**
     * 物品的额外 NBT
     */
    public final Map<String, String> nbtInts;
    /**
     * 左键点击物品执行的操作
     */
    public final List<IAction> leftClickCommands;
    /**
     * 右键点击物品执行的操作
     */
    public final List<IAction> rightClickCommands;
    /**
     * Shift+左键点击物品执行的操作
     */
    public final List<IAction> shiftLeftClickCommands;
    /**
     * Shift+右键点击物品执行的操作
     */
    public final List<IAction> shiftRightClickCommands;
    /**
     * 鼠标悬停，Q键点击物品执行的操作
     */
    public final List<IAction> dropCommands;
    /**
     * 物品的自定义标签，通过 ITagProvider 提供
     */
    public final Object tag;

    LoadedIcon(ConfigurationSection current) {
        ConfigurationSection section;

        String material, materialStr = current.getString("material");
        if (materialStr != null) {
            if (!materialStr.contains(":") && current.contains("data")) { // 兼容旧的选项
                material = materialStr + ":" + current.getInt("data");
            } else material = materialStr;
        } else material = "PAPER";
        this.material = material.toUpperCase();

        this.amount = current.getInt("amount", 1);
        this.display = current.getString("display", "");
        this.lore = current.getStringList("lore");
        this.glow = current.getBoolean("glow");
        this.customModelData = current.contains("custom-model-data") ? current.getInt("custom-model-data") : null;
        this.nbtStrings = new HashMap<>();
        section = current.getConfigurationSection("nbt-strings");
        if (section != null) for (String key : section.getKeys(false)) {
            nbtStrings.put(key, section.getString(key, ""));
        }
        this.nbtInts = new HashMap<>();
        section = current.getConfigurationSection("nbt-ints");
        if (section != null) for (String key : section.getKeys(false)) {
            nbtInts.put(key, section.getString(key, ""));
        }
        this.leftClickCommands = loadActions(current, "left-click-commands", "left_click_commands");
        this.rightClickCommands = loadActions(current, "right-click-commands", "right_click_commands");
        this.shiftLeftClickCommands = loadActions(current, "shift-left-click-commands", "shift_left_click_commands");
        this.shiftRightClickCommands = loadActions(current, "shift-right-click-commands", "shift_right_click_commands");
        this.dropCommands = loadActions(current, "drop-commands", "drop_commands");
        Object tag = null;
        for (ITagProvider provider : tagProviders) {
            if ((tag = provider.provide(current)) != null) {
                break;
            }
        }
        this.tag = tag;
    }

    /**
     * 生成一个新的物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @see LoadedIcon#generateIcon(ItemStack, Player, IModifier, IModifier)
     */
    public ItemStack generateIcon(Player player) {
        return generateIcon(player, null, null);
    }

    /**
     * 生成一个新的物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier 物品Lore修饰器
     * @see LoadedIcon#generateIcon(ItemStack, Player, IModifier, IModifier)
     */
    public ItemStack generateIcon(Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
        if (material.equals("AIR") || amount == 0) return new ItemStack(Material.AIR);
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(this.material);
        ItemStack item = pair == null ? new ItemStack(Material.PAPER) : ItemStackUtil.legacy(pair);
        return generateIcon(item, player, displayNameModifier, loreModifier);
    }

    /**
     * 基于已有物品，覆盖图标配置到该物品上。这个方法会忽略 <code>material</code> 选项。
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @return <code>item</code> 的引用
     * @see LoadedIcon#generateIcon(ItemStack, Player, IModifier, IModifier)
     */
    public ItemStack generateIcon(@Nullable ItemStack item, @Nullable Player player) {
        return generateIcon(item, player, null, null);
    }

    /**
     * 基于已有物品，覆盖图标配置到该物品上。这个方法会忽略 <code>material</code> 选项。
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier 物品Lore修饰器
     * @return 如果 <code>item</code> 不是 <code>null</code>，返回原物品的引用
     */
    public @NotNull ItemStack generateIcon(@Nullable ItemStack item, @Nullable Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
        if (item == null || amount == 0) return new ItemStack(Material.AIR);
        item.setAmount(amount);
        applyItemMeta(item, player, displayNameModifier, loreModifier);
        return item;
    }

    /**
     * 应用该图标配置中的 物品名、物品Lore、发光、自定义标记… 等元数据到指定物品
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @see LoadedIcon#applyItemMeta(ItemStack, Player, IModifier, IModifier)
     */
    public void applyItemMeta(@NotNull ItemStack item, @Nullable Player player) {
        applyItemMeta(item, player, null, null);
    }

    /**
     * 应用该图标配置中的 物品名、物品Lore、发光、自定义标记… 等元数据到指定物品
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier 物品Lore修饰器
     */
    public void applyItemMeta(@NotNull ItemStack item, @Nullable Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
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
    }

    /**
     * 处理图标点击操作
     * @param player 点击该图标的玩家
     * @param type 具体点击操作
     */
    public void click(@NotNull Player player, @NotNull ClickType type) {
        click(player, type, null);
    }

    /**
     * 处理图标点击操作
     * @param player 点击该图标的玩家
     * @param type 具体点击操作
     * @param replacements 替换变量列表
     */
    public void click(@NotNull Player player, @NotNull ClickType type, @Nullable List<Pair<String, Object>> replacements) {
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
        ListPair<String, Object> args = new ListPair<>();
        args.add("__internal__loaded_icon", this);
        if (replacements != null) args.addAll(replacements);
        for (IAction action : actions) {
            action.run(player, args);
        }
    }

    /**
     * 加载图标配置
     * @param section 配置根部分
     */
    public static @NotNull LoadedIcon load(@NotNull ConfigurationSection section) {
        return load(section, null);
    }

    /**
     * 加载图标配置
     * @param section 配置
     * @param id 配置根部分的位置键，若为 <code>null</code>，则配置根部分为 <code>section</code>
     */
    public static @NotNull LoadedIcon load(@NotNull ConfigurationSection section, @Nullable String id) {
        ConfigurationSection current = id == null ? section : section.getConfigurationSection(id);
        if (current == null) throw new IllegalArgumentException("Can't find root section of LoadedIcon");

        return new LoadedIcon(current);
    }

    /**
     * 注册 ITagProvider 标签提供器
     */
    public static void registerTagProvider(ITagProvider provider) {
        tagProviders.add(provider);
        tagProviders.sort(Comparator.comparingInt(ITagProvider::priority));
    }
}
