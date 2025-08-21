package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTReflectionUtil;
import de.tr7zw.changeme.nbtapi.utils.nmsmappings.ReflectionMethod;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

/**
 * 基于 adventure 的 Component 的物品操作工具
 */
public class AdventureItemStack {
    private static ItemEditor itemEditor;
    private static boolean supportCustomModelData = false;
    protected static void init(BukkitPlugin plugin) {
        try {
            ItemMeta.class.getDeclaredMethod("setCustomModelData", Integer.class);
            supportCustomModelData = true;
        } catch (Throwable ignored) {
        }
        itemEditor = plugin.initItemEditor();
    }

    /**
     * 设置物品编辑器，通常已在插件主类获取，不需要手动设置
     * @see BukkitPlugin#initItemEditor()
     */
    public static void setItemEditor(ItemEditor itemEditor) {
        AdventureItemStack.itemEditor = itemEditor;
    }

    /**
     * 获取物品编辑器，用于兼容在不同平台上，使用 adventure 的 Component 获取和设置物品名称、Lore 的方法
     */
    public static ItemEditor getItemEditor() {
        return itemEditor;
    }

    /**
     * 获取 Component 序列化器
     * @see ItemEditor#serializer()
     */
    public static ComponentSerializer<Component, ?, String> serializer() {
        return itemEditor.serializer();
    }

    /**
     * 快速构建一个物品
     * @param material 物品图标
     * @param name 物品名
     * @param lore 物品Lore
     * @see AdventureItemStack#buildItem(Material, Integer, String, List)
     */
    public static ItemStack buildItem(Material material, String name, String... lore) {
        return buildItem(material, null, name, Lists.newArrayList(lore));
    }

    /**
     * 快速构建一个物品
     * @param material 物品图标
     * @param name 物品名
     * @param lore 物品Lore
     * @see AdventureItemStack#buildItem(Material, Integer, String, List)
     */
    public static ItemStack buildItem(@NotNull Material material, @NotNull String name, @NotNull List<String> lore) {
        return buildItem(material, null, name, lore);
    }

    /**
     * 快速构建一个物品
     * @param material 物品图标
     * @param customModelData 自定义模型标记
     * @param name 物品名
     * @param lore 物品Lore
     * @see AdventureItemStack#buildItem(Material, Integer, String, List)
     */
    public static ItemStack buildItem(@NotNull Material material, @Nullable Integer customModelData, @NotNull String name, @NotNull String... lore) {
        return buildItem(material, customModelData, name, Lists.newArrayList(lore));
    }

    /**
     * 快速构建一个物品
     * @param material 物品图标
     * @param customModelData 自定义模型标记
     * @param name 物品名
     * @param lore 物品Lore
     */
    public static ItemStack buildItem(@NotNull Material material, @Nullable Integer customModelData, @NotNull String name, @NotNull List<String> lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
        setItemDisplayName(item, name);
        setItemLoreMiniMessage(item, lore);
        if (customModelData != null) {
            setCustomModelData(item, customModelData);
        }
        return item;
    }

    /**
     * 设置物品名，支持 MiniMessage
     * @param item 物品
     * @param name 名称
     * @see ItemEditor#setItemDisplayName(ItemStack, Component)
     */
    public static void setItemDisplayName(ItemStack item, String name) {
        if (isEmpty(item)) return;
        setItemDisplayName(item, miniMessage(name));
    }

    /**
     * 设置物品名
     * @param item 物品
     * @param name 名称
     * @see ItemEditor#setItemDisplayName(ItemStack, Component)
     */
    public static void setItemDisplayName(ItemStack item, Component name) {
        itemEditor.setItemDisplayName(item, name);
    }

    /**
     * 获取物品名为 MiniMessage
     * @param item 物品
     * @see ItemEditor#getItemDisplayName(ItemStack)
     */
    @Nullable
    public static String getItemDisplayNameAsMiniMessage(ItemStack item) {
        Component component = getItemDisplayName(item);
        if (component == null) return null;
        return AdventureUtil.miniMessage(component);
    }

    /**
     * 获取物品名
     * @param item 物品
     * @see ItemEditor#getItemDisplayName(ItemStack)
     */
    @Nullable
    public static Component getItemDisplayName(ItemStack item) {
        return itemEditor.getItemDisplayName(item);
    }

    /**
     * 设置物品 Lore，支持 MiniMessage
     * @param item 物品
     * @param lore Lore
     * @see ItemEditor#setItemLore(ItemStack, List)
     */
    public static void setItemLoreMiniMessage(ItemStack item, String... lore) {
        setItemLoreMiniMessage(item, Lists.newArrayList(lore));
    }

    /**
     * 设置物品 Lore，支持 MiniMessage
     * @param item 物品
     * @param lore Lore
     * @see ItemEditor#setItemLore(ItemStack, List)
     */
    public static void setItemLoreMiniMessage(ItemStack item, List<String> lore) {
        if (isEmpty(item)) return;
        List<Component> lines = new ArrayList<>();
        for (String s : lore) {
            lines.add(miniMessage(s));
        }
        setItemLore(item, lines);
    }

    /**
     * 设置物品 Lore
     * @param item 物品
     * @param lore Lore
     * @see ItemEditor#setItemLore(ItemStack, List)
     */
    public static void setItemLore(ItemStack item, List<Component> lore) {
        itemEditor.setItemLore(item, lore);
    }

    /**
     * 获取物品 Lore 为 MiniMessage
     * @param item 物品
     * @see ItemEditor#getItemLore(ItemStack)
     */
    @NotNull
    public static List<String> getItemLoreAsMiniMessage(ItemStack item) {
        List<Component> components = getItemLore(item);
        List<String> lore = new ArrayList<>();
        for (Component component : components) {
            String s = miniMessage(component);
            lore.add(s);
        }
        return lore;
    }

    /**
     * 获取物品 Lore
     * @param item 物品
     * @see ItemEditor#getItemLore(ItemStack)
     */
    @NotNull
    public static List<Component> getItemLore(ItemStack item) {
        return itemEditor.getItemLore(item);
    }

    /**
     * 设置物品的自定义模型标记，在 1.14 以下无效
     * @param item 物品
     * @param customModelData 自定义模型标记
     */
    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (!supportCustomModelData) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
    }

    /**
     * 将物品转换为鼠标悬停显示参数
     * @param item 物品
     */
    @SuppressWarnings({"deprecation"})
    public static HoverEventSource<?> toHoverEvent(ItemStack item) {
        // Paper 方案 - 直接转换
        if (item instanceof HoverEventSource) {
            return (HoverEventSource<?>) item;
        }
        // Spigot 方案 - 读取物品信息与 NBT
        // https://github.com/MrXiaoM/DeathMessages/blob/main/src/main/java/dev/mrshawn/deathmessages/utils/HoverShowItemResolver.java
        Object nmsItem = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, item);
        NBTContainer nbt = NBTReflectionUtil.convertNMSItemtoNBTCompound(nmsItem);
        NBTCompound components = nbt.hasTag("components") ? nbt.getCompound("components") : null;
        NBTCompound tag = nbt.hasTag("tag") ? nbt.getCompound("tag") : null;

        BinaryTagHolder itemTag;
        if (components != null) { // 1.21.5+
            itemTag = BinaryTagHolder.binaryTagHolder(components.toString());
        } else if (tag != null) { // 1.7-1.21.4
            itemTag = BinaryTagHolder.binaryTagHolder(tag.toString());
        } else { // 未知格式
            itemTag = BinaryTagHolder.binaryTagHolder("{}");
        }
        return HoverEvent.showItem(
                Key.key(nbt.getString("id"), ':'),
                nbt.getInteger("count"),
                itemTag);
    }

    public static MiniMessage.Builder wrapHoverEvent(ItemStack item) {
        return wrapHoverEvent("item", item);
    }

    public static MiniMessage.Builder wrapHoverEvent(String tagName, ItemStack item) {
        return AdventureUtil.builder()
                .editTags(it -> it.resolver(wrapHoverResolver(tagName, item)));
    }

    public static MiniMessage.Builder wrapHoverEvent(List<Pair<String, ItemStack>> items) {
        MiniMessage.Builder builder = AdventureUtil.builder();
        for (Pair<String, ItemStack> pair : items) {
            builder.editTags(it -> it.resolver(wrapHoverResolver(pair.key(), pair.value())));
        }
        return builder;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static TagResolver wrapHoverResolver(String tagName, ItemStack item) {
        return SerializableResolver.claimingStyle(
                tagName,
                (args, ctx) -> Tag.styling(toHoverEvent(item).asHoverEvent()),
                StyleClaim.claim(
                        tagName,
                        Style::hoverEvent,
                        (event, emitter) -> emitter.tag(tagName)
                ));
    }

    /**
     * 获取该物品是否为空物品
     */
    @Contract("null -> true")
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}
