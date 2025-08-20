package top.mrxiaom.pluginbase.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * adventure 与 mini message 相关操作工具
 */
public class AdventureUtil {
    private static BukkitAudiences adventure;
    private static MiniMessage miniMessage;

    @SuppressWarnings({"unchecked", "CallToPrintStackTrace"})
    private static void remove(TagResolver.Builder builder, String... tags) {
        Class<?> type = builder.getClass();
        try {
            Field field = type.getDeclaredField("resolvers");
            field.setAccessible(true);
            List<TagResolver> list = (List<TagResolver>) field.get(builder);
            list.removeIf(it -> {
                for (String tag : tags) {
                    if (it.has(tag)) return true;
                }
                return false;
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static MiniMessage create() {
        return MiniMessage.builder()
                .editTags(it -> remove(it, "pride"))
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
    }

    protected static void init(BukkitPlugin plugin) {
        adventure = BukkitAudiences.builder(plugin).build();
        miniMessage = create();
        try {
            AdventureItemStack.init(plugin);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 获取 adventure-platform-bukkit 的实例
     */
    public static BukkitAudiences adventure() {
        return adventure;
    }

    /**
     * 获取 Bukkit CommandSender 在 adventure 的 Audience，如果有本地平台实现，优先使用本地平台实现
     */
    public static Audience of(CommandSender sender) {
        if (sender instanceof Audience) {
            return (Audience) sender;
        }
        return adventure.sender(sender);
    }

    /**
     * @see AdventureUtil#of(CommandSender)
     */
    public static Audience of(Player player) {
        return of((CommandSender) player);
    }

    /**
     * @see AdventureUtil#of(CommandSender)
     */
    public static Audience of(UUID player) {
        return Util.getOnlinePlayer(player).map(AdventureUtil::of).orElseGet(() -> adventure.player(player));
    }

    /**
     * @see AdventureUtil#of(CommandSender)
     */
    public static Audience console() {
        return of(Bukkit.getConsoleSender());
    }

    /**
     * 获取 MiniMessage 实例
     */
    public static MiniMessage miniMessage() {
        return miniMessage;
    }

    /**
     * 将字符串通过 MiniMessage 转换为 Component
     */
    @NotNull
    public static Component miniMessage(@Nullable String s) {
        return s == null
                ? Component.empty()
                : miniMessage.deserialize(legacyToMiniMessage(s));
    }

    /**
     * 将 Component 通过 MiniMessage 转换为字符串
     */
    @NotNull
    public static String miniMessage(@Nullable Component component) {
        return component == null
                ? ""
                : miniMessage.serialize(component);
    }

    /**
     * 将字符串列表通过 MiniMessage 转换为 Component 列表
     */
    @NotNull
    public static List<Component> miniMessage(List<String> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<Component> components = new ArrayList<>();
        for (String s : list) {
            components.add(s == null
                    ? Component.empty()
                    : miniMessage.deserialize(legacyToMiniMessage(s)));
        }
        return components;
    }

    /**
     * 将字符串列表通过 MiniMessage 转换为 Component，列表每一项均为一行
     */
    public static Component miniMessageLines(List<String> list) {
        if (list == null || list.isEmpty()) return Component.empty();
        TextComponent.Builder text = Component.text();
        text.append(miniMessage(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            text.appendNewline();
            text.append(miniMessage(list.get(i)));
        }
        return text.build();
    }

    /**
     * 将 Component 列表通过 MiniMessage 转换为字符串列表
     */
    @NotNull
    public static List<String> miniMessage_(List<Component> components) {
        if (components == null) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (Component component : components) {
            list.add(miniMessage(component));
        }
        return list;
    }

    /**
     * 向玩家发送标题消息
     * @param player 玩家
     * @param title 大标题
     * @param subTitle 副标题
     * @param fadeIn 淡入时间 (tick)
     * @param stay 保持时间 (tick)
     * @param fadeOut 淡出时间 (tick)
     */
    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        of(player).showTitle(Title.title(
                miniMessage(title), miniMessage(subTitle), Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    /**
     * 重置玩家标题
     * @param player 玩家
     */
    public static void resetTitle(Player player) {
        of(player).resetTitle();
    }

    /**
     * 清空玩家标题
     * @param player 玩家
     */
    public static void clearTitle(Player player) {
        of(player).clearTitle();
    }

    /**
     * 向用户发送消息，支持 MiniMessage
     * @param sender 消息接收者
     * @param message 消息
     */
    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, miniMessage(message));
    }

    /**
     * 向用户发送消息
     * @param sender 消息接收者
     * @param message 消息
     */
    public static void sendMessage(CommandSender sender, Component message) {
        of(sender).sendMessage(message);
    }

    /**
     * 向玩家发送 ActionBar 消息，即物品栏上方的消息
     * @param player 玩家
     * @param message 消息
     */
    public static void sendActionBar(Player player, String message) {
        sendActionBar(player, miniMessage(message));
    }

    /**
     * 向玩家发送 ActionBar 消息
     * @param player 玩家
     * @param message 消息
     */
    public static void sendActionBar(Player player, Component message) {
        of(player).sendActionBar(message);
    }

    /**
     * 将过时的 <code>&</code>、<code>§</code> 样式代码转换为 MiniMessage 格式
     */
    public static String legacyToMiniMessage(String legacy) {
        StringBuilder builder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                builder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                builder.append(chars[i]);
                continue;
            }
            switch (Character.toLowerCase(chars[i+1])) {
                case '0': builder.append("<black>"); break;
                case '1': builder.append("<dark_blue>"); break;
                case '2': builder.append("<dark_green>"); break;
                case '3': builder.append("<dark_aqua>"); break;
                case '4': builder.append("<dark_red>"); break;
                case '5': builder.append("<dark_purple>"); break;
                case '6': builder.append("<gold>"); break;
                case '7': builder.append("<gray>"); break;
                case '8': builder.append("<dark_gray>"); break;
                case '9': builder.append("<blue>"); break;
                case 'a': builder.append("<green>"); break;
                case 'b': builder.append("<aqua>"); break;
                case 'c': builder.append("<red>"); break;
                case 'd': builder.append("<light_purple>"); break;
                case 'e': builder.append("<yellow>"); break;
                case 'f': builder.append("<white>"); break;
                case 'r': builder.append("<reset><!i>"); break;
                case 'l': builder.append("<b>"); break;
                case 'm': builder.append("<st>"); break;
                case 'o': builder.append("<i>"); break;
                case 'n': builder.append("<u>"); break;
                case 'k': builder.append("<obf>"); break;
                case 'x': {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        builder.append(chars[i]);
                        continue;
                    }
                    builder
                            .append("<#")
                            .append(chars[i+3])
                            .append(chars[i+5])
                            .append(chars[i+7])
                            .append(chars[i+9])
                            .append(chars[i+11])
                            .append(chars[i+13])
                            .append(">");
                    i += 12;
                    break;
                }
                default: {
                    builder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return builder.toString();
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }
}
