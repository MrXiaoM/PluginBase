package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTReflectionUtil;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import de.tr7zw.changeme.nbtapi.utils.nmsmappings.ReflectionMethod;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.item.LegacyItemEditor;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class AdventureItemStack {
    private static ItemEditor itemEditor;
    protected static void init(BukkitPlugin plugin) {
        itemEditor = plugin.initItemEditor();
    }

    public static void setItemEditor(ItemEditor itemEditor) {
        AdventureItemStack.itemEditor = itemEditor;
    }

    public static ItemEditor getItemEditor() {
        return itemEditor;
    }

    public static ComponentSerializer<Component, ?, String> serializer() {
        return itemEditor.serializer();
    }

    public static ItemStack buildItem(Material material, String name, String... lore) {
        return buildItem(material, null, name, Lists.newArrayList(lore));
    }
    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, String... lore) {
        return buildItem(material, customModelData, name, Lists.newArrayList(lore));
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, List<String> lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
        setItemDisplayName(item, name);
        setItemLoreMiniMessage(item, lore);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (isEmpty(item)) return;
        setItemDisplayName(item, miniMessage(name));
    }

    public static void setItemDisplayName(ItemStack item, Component name) {
        itemEditor.setItemDisplayName(item, name);
    }

    @Nullable
    public static String getItemDisplayNameAsMiniMessage(ItemStack item) {
        Component component = getItemDisplayName(item);
        if (component == null) return null;
        return AdventureUtil.miniMessage(component);
    }

    @Nullable
    public static Component getItemDisplayName(ItemStack item) {
        return itemEditor.getItemDisplayName(item);
    }

    @Nullable
    @Deprecated
    public static String getItemDisplayNameAsJson(ItemStack item) {
        return LegacyItemEditor.getItemDisplayNameAsJson(item);
    }

    @Deprecated
    public static void setItemDisplayNameByJson(ItemStack item, String json) {
        LegacyItemEditor.setItemDisplayNameByJson(item, json);
    }

    public static void setItemLoreMiniMessage(ItemStack item, String... lore) {
        setItemLoreMiniMessage(item, Lists.newArrayList(lore));
    }

    public static void setItemLoreMiniMessage(ItemStack item, List<String> lore) {
        if (isEmpty(item)) return;
        List<Component> lines = new ArrayList<>();
        for (String s : lore) {
            lines.add(miniMessage(s));
        }
        setItemLore(item, lines);
    }

    public static void setItemLore(ItemStack item, List<Component> lore) {
        itemEditor.setItemLore(item, lore);
    }

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

    @NotNull
    public static List<Component> getItemLore(ItemStack item) {
        return itemEditor.getItemLore(item);
    }

    @Nullable
    @Deprecated
    public static List<String> getItemLoreAsJson(ItemStack item) {
        return LegacyItemEditor.getItemLoreAsJson(item);
    }

    @Deprecated
    public static void setItemLoreByJson(ItemStack item, List<String> json) {
        LegacyItemEditor.setItemLoreByJson(item, json);
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
    }

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

    @Contract("null -> true")
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}
