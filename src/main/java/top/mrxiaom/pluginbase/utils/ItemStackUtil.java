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
import org.jetbrains.annotations.Nullable;
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
        Enchantment enchant = Util.valueOrNull(Enchantment.class, "DURABILITY", "UNBREAKING");
        if (enchant != null) {
            ItemMeta meta = getItemMeta(item);
            meta.addEnchant(enchant, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
    }

    @NotNull
    @SuppressWarnings("DataFlowIssue")
    public static ItemStack getItem(String str) {
        return getItem(str, false);
    }

    @Nullable
    public static ItemStack getItem(String str, boolean defNull) {
        if (str.startsWith("itemsadder-")) {
            Optional<ItemStack> item = IA.get(str.substring(11));
            if (defNull && !item.isPresent()) return null;
            return item.orElseThrow(
                    () -> new IllegalStateException("找不到 IA 物品 " + str.substring(11))
            );
        } else if (str.startsWith("head-base64-")) {
            ItemStack item = SkullsUtil.createHeadItem();
            String base64 = str.substring(12);
            ItemMeta meta = SkullsUtil.setSkullBase64(item.getItemMeta(), base64);
            if (meta != null) {
                item.setItemMeta(meta);
            }
            return item;
        } else {
            Integer customModelData = null;
            String material = str;
            if (str.contains("#")) {
                String customModel = str.substring(str.indexOf("#") + 1);
                customModelData = Util.parseInt(customModel).orElse(null);
                material = str.replace("#" + customModelData, "");
            }
            Pair<Material, Integer> pair = parseMaterial(material);
            if (pair == null) {
                if (defNull) return null;
                throw new IllegalStateException("找不到物品 " + material);
            }
            ItemStack item = legacy(pair);
            if (customModelData != null) try  {
                ItemMeta meta = ItemStackUtil.getItemMeta(item);
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            } catch (Throwable ignored) {
            }
            return item;
        }
    }

    private static final String[] materialColors = new String[] {
            "STAINED_GLASS", "STAINED_GLASS_PANE", "WOOL", "CARPET"
    };
    private static final String[] dataValueColors = new String[] {
            "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
            "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
    };
    private static final String[] dataValueSkulls = new String[] {
            "SKELETON_SKULL", "WITHER_SKELETON_SKULL", "ZOMBIE_HEAD", "PLAYER_HEAD", "CREEPER_HEAD",
            "SKELETON_WALL_SKULL", "WITHER_SKELETON_WALL_SKULL", "ZOMBIE_WALL_HEAD", "PLAYER_WALL_HEAD", "CREEPER_WALL_HEAD",
    };
    @Nullable
    public static Pair<Material, Integer> parseMaterial(String input) {
        String str;
        Integer dataValue;
        if (input.contains(":")) {
            String[] split = input.split(":", 2);
            str = split[0];
            dataValue = Util.parseInt(split[1]).orElse(null);
        } else {
            str = input;
            dataValue = null;
        }
        // 可正常处理的物品优先
        Material material = Material.getMaterial(str.toUpperCase());
        if (material == null) {
            material = parseOrNull(str);
        }
        if (dataValue != null) {
            if (material == null) return null;
            return Pair.of(material, dataValue);
        }
        if (material != null) {
            return Pair.of(material, null);
        }
        // 以下均为 1.13+ 物品ID转为 1.12 及以下物品ID
        // 带颜色的物品
        if (str.endsWith("_CONCRETE") || str.endsWith("_TERRACOTTA")) {
            // 混凝土、陶瓦 -> 硬化粘土
            material = parseOrNull("STAINED_HARDENED_CLAY");
        } else if (str.contains("BANNER") && !str.contains("PATTERN")) {
            // 旗帜
            material = parseOrNull("BANNER");
        } else if (str.endsWith("DYE")) {
            // 染料
            material = parseOrNull("INK_SACK");
        } else for (String mc : materialColors) {
            // 其它ID比较规则的有颜色方块
            if (str.endsWith(mc)) {
                material = parseOrNull(mc);
                break;
            }
        }
        // 如果ID符合以上规则，则读取颜色，并返回
        if (material != null) {
            Integer data = null;
            boolean reverse = str.endsWith("DYE");
            for (int i = 0; i < dataValueColors.length; i++) {
                if (str.startsWith(dataValueColors[i])) {
                    data = reverse ? (15 - i) : i;
                    break;
                }
            }
            return Pair.of(material, data);
        }
        // 头颅
        int skullData = 3;
        for (int i = 0; i < dataValueSkulls.length; i++) {
            if (str.equals(dataValueSkulls[i])) {
                material = parseOrNull("SKULL_ITEM");
                skullData = i % 5;
                break;
            }
        }
        if (material != null) {
            return Pair.of(material, skullData);
        }
        // 其它杂项物品
        Integer data = null;
        if (str.contains("_HEAD")) { // 旧版没有的头颅，一律转为玩家头颅
            material = parseOrNull("SKULL_ITEM");
            if (material != null) data = 3;
        }
        if (material == null && str.equals("CLOCK")) material = parseOrNull("WATCH");
        if (material == null && str.contains("BED")) material = parseOrNull("BED");
        if (material == null && str.equals("CRAFT_TABLE")) material = parseOrNull("WORKBENCH");
        if (material == null && str.startsWith("WOODEN_")) material = parseOrNull(str.replace("WOODEN_", "WOOD_"));
        if (material == null && str.contains("_DOOR") && !str.contains("IRON")) material = parseOrNull("WOOD_DOOR");
        if (material == null && str.equals("IRON_BARS")) material = parseOrNull("IRON_FENCE");
        if (material == null && str.equals("BUNDLE")) material = parseOrNull("FEATHER");
        if (material == null && str.equals("ENDER_EYE")) material = parseOrNull("EYE_OF_ENDER");
        if (material == null && str.equals("COMMAND_BLOCK")) material = parseOrNull("COMMAND");
        if (material == null && str.equals("COMMAND_BLOCK_MINECART")) material = parseOrNull("COMMAND_MINECART");
        if (material == null && str.equals("CHAIN_COMMAND_BLOCK")) material = parseOrNull("COMMAND_CHAIN");
        if (material == null && str.equals("REPEATING_COMMAND_BLOCK")) material = parseOrNull("COMMAND_REPEATING");
        // TODO: 支持更多新旧版本的物品转换
        if (material != null) {
            return Pair.of(material, data);
        }
        return null;
    }
    @Nullable
    public static Material parseOrNull(String str) {
        return Util.valueOr(Material.class, str, null);
    }

    @SuppressWarnings({"deprecation"})
    public static ItemStack legacy(Pair<Material, Integer> pair) {
        Material material = pair.getKey();
        Integer dataValue = pair.getValue();
        if (dataValue != null) {
            return new ItemStack(material, 1, dataValue.shortValue());
        } else {
            return new ItemStack(material);
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
