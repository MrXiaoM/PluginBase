package top.mrxiaom.pluginbase.utils.item;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.handler.NBTHandlers;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;

import java.util.ArrayList;
import java.util.List;

public class LegacyItemEditor implements ItemEditor {
    private final boolean textUseComponent;
    private static boolean itemNbtUseComponentsFormat;
    private static boolean componentUseNBT;
    public LegacyItemEditor() {
        itemNbtUseComponentsFormat = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        String testDisplayName = "§a§l测试§e§l文本";
        if (meta == null) { // 预料之外的情况
            textUseComponent = false;
            componentUseNBT = false;
        } else {
            meta.setDisplayName(testDisplayName);
            item.setItemMeta(meta);
            if (itemNbtUseComponentsFormat) {
                textUseComponent = true;
                componentUseNBT = NBT.getComponents(item, nbt -> { // 1.21.5 开始，文本组件从 JSON 字符串改为了 NBT 组件
                    NBTType type = nbt.getType("minecraft:custom_name");
                    return !type.equals(NBTType.NBTTagString);
                });
            } else {
                // 测试物品是否支持使用 component
                textUseComponent = NBT.get(item, nbt -> {
                    ReadableNBT display = nbt.getCompound("display");
                    if (display == null) {
                        return false;
                    }
                    String name = display.getString("Name");
                    // 旧版本文本组件不支持 JSON 字符串，设置旧版颜色符之后，物品名会跟之前一样
                    return !name.equals(testDisplayName);
                });
                componentUseNBT = false;
            }
        }
    }

    @Override
    public ComponentSerializer<Component, ?, String> serializer() {
        if (textUseComponent) {
            return GsonComponentSerializer.gson();
        } else {
            return LegacyComponentSerializer.legacySection();
        }
    }

    @Override
    public @Nullable Component getItemDisplayName(ItemStack item) {
        String nameAsJson = getItemDisplayNameAsJson(item);
        if (nameAsJson == null) return null;
        return serializer().deserialize(nameAsJson);
    }

    @Override
    public void setItemDisplayName(ItemStack item, @Nullable Component displayName) {
        if (AdventureItemStack.isEmpty(item)) return;
        setItemDisplayNameByJson(item, displayName == null ? null : serializer().serialize(displayName));
    }

    @Override
    public @NotNull List<Component> getItemLore(ItemStack item) {
        List<String> loreAsJson = getItemLoreAsJson(item);
        if (loreAsJson == null) return new ArrayList<>();
        List<Component> lore = new ArrayList<>();
        for (String line : loreAsJson) {
            Component component = serializer().deserialize(line);
            lore.add(component);
        }
        return lore;
    }

    @Override
    public void setItemLore(ItemStack item, @Nullable List<Component> lore) {
        if (AdventureItemStack.isEmpty(item)) return;
        List<String> json = new ArrayList<>();
        if (lore != null) for (Component component : lore) {
            json.add(serializer().serialize(component));
        }
        setItemLoreByJson(item, json);
    }


    @Nullable
    public static String getItemDisplayNameAsJson(ItemStack item) {
        if (AdventureItemStack.isEmpty(item)) return null;
        if (itemNbtUseComponentsFormat) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            if (nbt == null) return null;
            if (componentUseNBT) {
                ReadWriteNBT component = nbt.hasTag("minecraft:custom_name")
                        ? nbt.getCompound("minecraft:custom_name")
                        : null;
                return component != null
                        ? component.toString()
                        : null;
            } else {
                return nbt.hasTag("minecraft:custom_name", NBTType.NBTTagString)
                        ? nbt.getString("minecraft:custom_name")
                        : null;
            }
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
        if (itemNbtUseComponentsFormat) {
            NBT.modifyComponents(item, nbt -> {
                if (json == null) {
                    nbt.removeKey("minecraft:custom_name");
                    return;
                }
                if (componentUseNBT) {
                    ReadWriteNBT component = NBT.parseNBT(json);
                    nbt.set("minecraft:custom_name", component, NBTHandlers.STORE_READWRITE_TAG);
                } else {
                    nbt.setString("minecraft:custom_name", json);
                }
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                if (json == null) {
                    nbt.removeKey("Name");
                    return;
                }
                display.setString("Name", json);
            });
        }
    }

    @Nullable
    public static List<String> getItemLoreAsJson(ItemStack item) {
        if (AdventureItemStack.isEmpty(item)) return null;
        if (itemNbtUseComponentsFormat) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            if (nbt == null) return null;
            if (componentUseNBT) {
                ReadWriteNBTCompoundList components = nbt.hasTag("minecraft:custom_name")
                        ? nbt.getCompoundList("minecraft:custom_name")
                        : null;
                if (components == null) return null;
                List<String> list = new ArrayList<>();
                for (ReadWriteNBT component : components) {
                    list.add(component.toString());
                }
                return list;
            } else {
                return nbt.hasTag("minecraft:custom_name", NBTType.NBTTagList)
                        ? nbt.getStringList("minecraft:lore").toListCopy()
                        : null;
            }
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
        if (itemNbtUseComponentsFormat) {
            NBT.modifyComponents(item, nbt -> {
                if (componentUseNBT) {
                    ReadWriteNBTCompoundList list = nbt.getCompoundList("minecraft:lore");
                    if (!list.isEmpty()) list.clear();
                    for (String s : json) {
                        ReadWriteNBT component = NBT.parseNBT(s);
                        list.addCompound(component);
                    }
                } else {
                    ReadWriteNBTList<String> list = nbt.getStringList("minecraft:lore");
                    if (!list.isEmpty()) list.clear();
                    list.addAll(json);
                }
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
}
