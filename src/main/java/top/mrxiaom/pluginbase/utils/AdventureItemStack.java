package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class AdventureItemStack {
    private static boolean itemUseComponent;
    protected static void init() {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            itemUseComponent = true;
        } else {
            // 测试物品是否支持使用 component
            ItemStack item = new ItemStack(Material.STONE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String testDisplayName = "§a§l测试§e§l文本";
                meta.setDisplayName(testDisplayName);
                item.setItemMeta(meta);
                NBT.get(item, nbt -> {
                    ReadableNBT display = nbt.getCompound("display");
                    if (display == null) {
                        itemUseComponent = false;
                        return;
                    }
                    String name = display.getString("Name");
                    itemUseComponent = !name.equals(testDisplayName);
                });
            } else {
                itemUseComponent = false;
            }
        }
    }
    public static ComponentSerializer<Component, ?, String> serializer() {
        if (itemUseComponent) {
            return GsonComponentSerializer.gson();
        } else {
            return LegacyComponentSerializer.legacySection();
        }
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
        if (item == null || !item.hasItemMeta()) return;
        Component displayName = miniMessage(name);
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            String json = GsonComponentSerializer.gson().serialize(displayName);
            setItemDisplayNameByJson(item, json);
        } else {
            String legacy = LegacyComponentSerializer.legacySection().serialize(displayName);
            setItemDisplayNameByJson(item, legacy);
        }
    }

    public static void setItemDisplayName(ItemStack item, Component name) {
        if (item == null || !item.hasItemMeta()) return;
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            String json = GsonComponentSerializer.gson().serialize(name);
            setItemDisplayNameByJson(item, json);
        } else {
            String legacy = LegacyComponentSerializer.legacySection().serialize(name);
            setItemDisplayNameByJson(item, legacy);
        }
    }

    @Nullable
    public String getItemDisplayNameAsMiniMessage(ItemStack item) {
        Component component = getItemDisplayName(item);
        if (component == null) return null;
        return AdventureUtil.miniMessage(component);
    }

    @Nullable
    public Component getItemDisplayName(ItemStack item) {
        String nameAsJson = getItemDisplayNameAsJson(item);
        if (nameAsJson == null) return null;
        return GsonComponentSerializer.gson().deserialize(nameAsJson);
    }

    @Nullable
    public static String getItemDisplayNameAsJson(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            return nbt != null && nbt.hasTag("minecraft:custom_name", NBTType.NBTTagString)
                    ? nbt.getString("minecraft:custom_name")
                    : null;
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Name", NBTType.NBTTagString)
                        ? display.getString("Name")
                        : null;
            });
        }
    }

    public static void setItemDisplayNameByJson(ItemStack item, String json) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                nbt.setString("minecraft:custom_name", json);
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                display.setString("Name", json);
            });
        }
    }

    public static void setItemLoreMiniMessage(ItemStack item, String... lore) {
        setItemLoreMiniMessage(item, Lists.newArrayList(lore));
    }

    public static void setItemLoreMiniMessage(ItemStack item, List<String> lore) {
        if (item == null || !item.hasItemMeta()) return;
        List<String> json = new ArrayList<>();
        for (String s : lore) {
            Component line = miniMessage(s);
            json.add(serializer().serialize(line));
        }
        setItemLoreByJson(item, json);
    }

    public static void setItemLore(ItemStack item, List<Component> lore) {
        if (item == null || !item.hasItemMeta()) return;
        List<String> json = new ArrayList<>();
        for (Component component : lore) {
            json.add(serializer().serialize(component));
        }
        setItemLoreByJson(item, json);
    }

    @Nullable
    public static List<String> getItemLoreAsMiniMessage(ItemStack item) {
        List<Component> components = getItemLore(item);
        if (components == null) return null;
        List<String> lore = new ArrayList<>();
        for (Component component : components) {
            String s = miniMessage(component);
            lore.add(s);
        }
        return lore;
    }

    @Nullable
    public static List<Component> getItemLore(ItemStack item) {
        List<String> loreAsJson = getItemLoreAsJson(item);
        if (loreAsJson == null) return null;
        List<Component> lore = new ArrayList<>();
        for (String line : loreAsJson) {
            Component component = GsonComponentSerializer.gson().deserialize(line);
            lore.add(component);
        }
        return lore;
    }

    @Nullable
    public static List<String> getItemLoreAsJson(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            return nbt != null && nbt.hasTag("minecraft:custom_name", NBTType.NBTTagList)
                    ? nbt.getStringList("minecraft:lore").toListCopy()
                    : null;
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Lore", NBTType.NBTTagList)
                        ? display.getStringList("Lore").toListCopy()
                        : null;
            });
        }
    }

    public static void setItemLoreByJson(ItemStack item, List<String> json) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                ReadWriteNBTList<String> list = nbt.getStringList("minecraft:lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                ReadWriteNBTList<String> list = display.getStringList("Lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
            });
        }
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
    }
}
