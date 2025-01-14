package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import top.mrxiaom.pluginbase.BukkitPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class ItemStackUtil {
    private static final Integer[] frameSlots54 = new Integer[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9,/* 10, 11, 12, 13, 14, 15, 16, */17,
            18, /* 19, 20, 21, 22, 23, 24, 25, */26,
            27,/* 28, 29, 30, 31, 32, 33, 34, */35,
            36, /* 37, 38, 39, 40, 41, 42, 43, */44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    };
    private static final Integer[] frameSlots45 = new Integer[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9,/* 10, 11, 12, 13, 14, 15, 16, */17,
            18,/* 19, 20, 21, 22, 23, 24, 25, */26,
            27,/* 28, 29, 30, 31, 32, 33, 34, */35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    private static final Integer[] frameSlots36 = new Integer[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9,/* 10, 11, 12, 13, 14, 15, 16, */17,
            18,/* 19, 20, 21, 22, 23, 24, 25, */26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };
    private static final Integer[] frameSlots27 = new Integer[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9,/* 10, 11, 12, 13, 14, 15, 16, */17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    public static String serializeItem(ItemStack item) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
                out.writeObject(item);
            }
            return Base64Coder.encodeLines(bytes.toByteArray());
        } catch (Throwable t) {
            BukkitPlugin.getInstance().warn("序列化物品时出现一个错误", t);
            return null;
        }
    }

    public static String serializeItems(ItemStack[] items) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
                out.writeInt(items.length);
                for (ItemStack item : items) {
                    out.writeObject(item);
                }
            }
            return Base64Coder.encodeLines(bytes.toByteArray());
        } catch (Throwable t) {
            BukkitPlugin.getInstance().warn("序列化物品时出现一个错误", t);
            return null;
        }
    }

    public static ItemStack deserializeItem(String s) {
        if (s.trim().isEmpty()) return null;
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(Base64Coder.decodeLines(s))) {
            try (BukkitObjectInputStream in = new BukkitObjectInputStream(bytes)) {
                return (ItemStack) in.readObject();
            }
        } catch (Throwable t) {
            BukkitPlugin.getInstance().warn("反序列化物品时出现一个错误", t);
            return null;
        }
    }

    public static ItemStack[] deserializeItems(String s) {
        if (s.trim().isEmpty()) return new ItemStack[0];
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(Base64Coder.decodeLines(s))) {
            try (BukkitObjectInputStream in = new BukkitObjectInputStream(bytes)) {
                ItemStack[] items = new ItemStack[in.readInt()];
                for (int i = 0; i < items.length; ++i) {
                    items[i] = (ItemStack) in.readObject();
                }
                return items;
            }
        } catch (Throwable t) {
            BukkitPlugin.getInstance().warn("反序列化物品时出现一个错误", t);
            return new ItemStack[0];
        }
    }

    public static String getItemDisplayName(ItemStack item) {
        if ((item == null) || !item.hasItemMeta() || item.getItemMeta() == null)
            return item != null ? item.getType().name() : "";
        return item.getItemMeta().getDisplayName();
    }

    public static List<String> getItemLore(ItemStack item) {
        if ((item == null) || !item.hasItemMeta() || item.getItemMeta() == null
                || (item.getItemMeta().getLore() == null))
            return new ArrayList<>();
        return item.getItemMeta().getLore();
    }

    public static void reduceItemInMainHand(Player player) {
        reduceItemInMainHand(player, 1);
    }

    public static void reduceItemInMainHand(Player player, int amount) {
        ItemStack im = player.getInventory().getItemInMainHand();
        if (im.getAmount() - amount > 0) {
            im.setAmount(im.getAmount() - amount);
            player.getInventory().setItemInMainHand(im);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    public static void reduceItemInOffHand(Player player) {
        reduceItemInOffHand(player, 1);
    }

    public static void reduceItemInOffHand(Player player, int amount) {
        ItemStack im = player.getInventory().getItemInOffHand();
        if (im.getAmount() - amount > 0) {
            im.setAmount(im.getAmount() - amount);
            player.getInventory().setItemInOffHand(im);
        } else {
            player.getInventory().setItemInOffHand(null);
        }
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (item == null)
            return;
        ItemMeta im = item.getItemMeta() == null ? getItemMeta(item.getType()) : item.getItemMeta();
        if (im == null)
            return;
        im.setDisplayName(ColorHelper.parseColor(name));
        item.setItemMeta(im);
    }

    public static void setItemLore(ItemStack item, String... lore) {
        setItemLore(item, Lists.newArrayList(lore));
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        if (item == null)
            return;
        ItemMeta im = item.getItemMeta() == null ? getItemMeta(item.getType()) : item.getItemMeta();
        if (im == null)
            return;
        List<String> newLore = new ArrayList<>();
        lore.forEach(s -> {
            if (s != null) newLore.add(ColorHelper.parseColor(s));
        });
        im.setLore(newLore);
        item.setItemMeta(im);
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
        } catch (Throwable ignored) {
        }
    }

    public static ItemStack buildFrameItem(Material material) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        return buildItem(material, "&f&l*", Lists.newArrayList());
    }

    public static ItemStack buildItem(Material material, String name) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        return buildItem(material, null, name, Lists.newArrayList());
    }

    public static ItemStack buildItem(Material material, String name, String... lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        return buildItem(material, null, name, Lists.newArrayList(lore));
    }
    public static ItemStack buildItem(Material material, Integer customModeData, String name, String... lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        return buildItem(material, customModeData, name, Lists.newArrayList(lore));
    }

    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, List<String> lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
        ItemMeta im = getItemMeta(material);
        im.setDisplayName(ColorHelper.parseColor(name));
        if (!lore.isEmpty()) {
            im.setLore(ColorHelper.parseColor(lore));
        }
        if (customModelData != null) {
            im.setCustomModelData(customModelData);
        }
        item.setItemMeta(im);
        return item;
    }

    public static void setGlow(ItemStack item) {
        ItemMeta meta = getItemMeta(item);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static ItemStack getItem(String str) {
        if (str.startsWith("itemsadder-")) {
            return IA.get(str.substring(11)).orElseThrow(
                    () -> new IllegalStateException("找不到 IA 物品 " + str.substring(11))
            );
        } else {
            Integer customModelData = null;
            String material = str;
            if (str.contains("#")) {
                String customModel = str.substring(str.indexOf("#") + 1);
                customModelData = Util.parseInt(customModel).orElseThrow(
                        () -> new IllegalStateException("无法解析 " + customModel + " 为整数")
                );
                material = str.replace("#" + customModelData, "");
            }
            Material m = Util.valueOr(Material.class, material, null);
            if (m == null) throw new IllegalStateException("找不到物品 " + str);
            ItemStack item = new ItemStack(m);
            if (customModelData != null) {
                ItemMeta meta = ItemStackUtil.getItemMeta(item);
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
            return item;
        }
    }

    @NotNull
    public static ItemMeta getItemMeta(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? getItemMeta(item.getType()) : meta;
    }

    public static ItemMeta getItemMeta(Material material) {
        return Bukkit.getItemFactory().getItemMeta(material);
    }

    public static void setFrameItems(Inventory inv, ItemStack item) {
        Integer[] frameSlots;
        switch (inv.getSize()) {
            case 27:
                frameSlots = frameSlots27;
                break;
            case 36:
                frameSlots = frameSlots36;
                break;
            case 45:
                frameSlots = frameSlots45;
                break;
            case 54:
                frameSlots = frameSlots54;
                break;
            default:
                return;
        }
        for (int slot : frameSlots) {
            inv.setItem(slot, item);
        }
    }

    public static void setRowItems(Inventory inv, int row, ItemStack item) {
        for (int i = 0; i < 9; i++) {
            inv.setItem(((row - 1) * 9) + i, item);
        }
    }

    public static ItemStack getEnchantedBook(Enchantment enchantment, int level) {
        Map<Enchantment, Integer> map = new HashMap<>();
        map.put(enchantment, level);
        return getEnchantedBook(map);
    }

    public static ItemStack getEnchantedBook(Map<Enchantment, Integer> map) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta im = item.getItemMeta();
        if (im == null)
            return item;
        for (Enchantment enchantment : map.keySet()) {
            ((org.bukkit.inventory.meta.EnchantmentStorageMeta) im).addStoredEnchant(enchantment, map.get(enchantment), true);
        }
        item.setItemMeta(im);
        return item;
    }

    public static void giveItemToPlayer(final Player player, final List<ItemStack> items) {
        giveItemToPlayer(player, items.toArray(new ItemStack[0]));
    }

    public static void giveItemToPlayer(final Player player, final ItemStack... items) {
        giveItemToPlayer(player, "", "", items);
    }

    public static void giveItemToPlayer(final Player player, final String msg, final String msgFull,
                                        final List<ItemStack> items) {
        giveItemToPlayer(player, msg, msgFull, items.toArray(new ItemStack[0]));
    }

    public static void giveItemToPlayer(final Player player, final String msg, final String msgFull,
                                        final ItemStack... items) {
        final Collection<ItemStack> last = player.getInventory().addItem(items).values();
        if (!msg.isEmpty() || (!last.isEmpty() && !msgFull.isEmpty())) {
            t(player, msg + (last.isEmpty() ? "" : ("\n&r" + msgFull)));
        }
        for (final ItemStack item : last) {
            player.getWorld().dropItem(player.getLocation(), item);
        }
    }

}
