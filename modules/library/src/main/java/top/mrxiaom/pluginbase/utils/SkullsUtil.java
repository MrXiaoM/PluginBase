package top.mrxiaom.pluginbase.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 玩家透露相关工具
 */
public class SkullsUtil {
    public static class Skull {
        private final Consumer<SkullMeta> applier;
        public Skull(Consumer<SkullMeta> applier) {
            this.applier = applier;
        }
        public void setSkull(SkullMeta meta) {
            applier.accept(meta);
        }
    }
    private static final Map<String, Skull> cached = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static boolean HAS_PLAYER_PROFILES = false;
    private static ItemStack headItem;
    @SuppressWarnings({"deprecation"})
    protected static void init() {
        try {
            Bukkit.class.getDeclaredMethod("createPlayerProfile", UUID.class);
            HAS_PLAYER_PROFILES = true;
        } catch (Throwable ignored) {
        }
        if (Util.valueOr(Material.class, "PLAYER_HEAD", null) != null) {
            headItem = new ItemStack(Material.PLAYER_HEAD, 1);
        } else {
            headItem = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }
    }

    /**
     * 创建一个玩家头颅物品
     */
    public static ItemStack createHeadItem() {
        return headItem.clone();
    }

    /**
     * 通过 base64 设置玩家头颅材质
     * @param meta 物品 ItemMeta
     * @param base64 头颅材质
     */
    public static ItemMeta setSkullBase64(ItemMeta meta, String base64) {
        if (meta instanceof SkullMeta) {
            Skull skull = getOrCreateSkull(base64);
            if (skull != null) {
                skull.setSkull((SkullMeta) meta);
            }
        }
        return meta;
    }

    /**
     * 获取或创建头颅缓存，有可能会创建失败
     */
    @Nullable
    public static Skull getOrCreateSkull(@NotNull String base64) {
        Skull skull = cached.get(base64);
        if (skull == null) {
            skull = generateSkull(base64);
            if (skull != null) {
                cached.put(base64, skull);
            }
        }
        return skull;
    }

    /**
     * 根据 Base64 生成头颅缓存
     */
    @Nullable
    public static Skull generateSkull(@NotNull String base64) {
        if (base64.isEmpty()) return null;
        if (HAS_PLAYER_PROFILES) {
            PlayerProfile profile = getPlayerProfile(base64);
            return new Skull(meta -> meta.setOwnerProfile(profile));
        }
        GameProfile profile = getGameProfile(base64);
        return new Skull(meta -> {
            Field field;
            try {
                field = meta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(meta, profile);
            } catch (ReflectiveOperationException e) {
                BukkitPlugin.getInstance().warn("无法从 base64 加载头颅材质", e);
            }
        });
    }

    @NotNull
    private static GameProfile getGameProfile(@NotNull String base64) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64));
        return profile;
    }

    @NotNull
    private static PlayerProfile getPlayerProfile(@NotNull String base64) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        String decodedBase64 = decodeSkinUrl(base64);
        if (decodedBase64 == null) return profile;

        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(decodedBase64));
        } catch (MalformedURLException e) {
            BukkitPlugin.getInstance().warn("尝试创建 base64 头颅材质链接时出现问题", e);
        }

        profile.setTextures(textures);
        return profile;
    }

    @Nullable
    private static String decodeSkinUrl(@NotNull String base64) {
        String decoded = new String(Base64.getDecoder().decode(base64));
        JsonObject object = GSON.fromJson(decoded, JsonObject.class);

        JsonElement textures = object.get("textures");
        if (textures == null || !textures.isJsonObject()) return null;

        JsonElement skin = textures.getAsJsonObject().get("SKIN");
        if (skin == null || !skin.isJsonObject()) return null;

        JsonElement url = skin.getAsJsonObject().get("url");
        return url == null ? null : url.getAsString();
    }
}
