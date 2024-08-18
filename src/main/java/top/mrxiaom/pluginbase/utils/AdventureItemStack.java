package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class AdventureItemStack {

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
        setItemLore(item, lore);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (item == null) return;
        Component displayName = miniMessage(name);
        String json = GsonComponentSerializer.gson().serialize(displayName);
        setItemDisplayNameByJson(item, json);
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

    public static void setItemLore(ItemStack item, String... lore) {
        setItemLore(item, Lists.newArrayList(lore));
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        if (item == null) return;
        List<String> json = new ArrayList<>();
        for (String s : lore) {
            Component line = miniMessage(s);
            json.add(GsonComponentSerializer.gson().serialize(line));
        }
        setItemLoreByJson(item, json);
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
        }
    }
}
