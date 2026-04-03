package top.mrxiaom.pluginbase.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAdventureHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * adventure 与 mini message 相关操作工具
 */
public class AdventureUtil {
    private static IAdventureHandler handler;

    /**
     * 创建 MiniMessage.Builder，添加了一些方便的设置，例如移除部分标签、自动转换旧版颜色代码等等
     */
    public static MiniMessage.Builder builder() {
        return handler.builder();
    }

    protected static void init(BukkitPlugin plugin) {
        try {
            handler = plugin.initAdventureHandler();
        } catch (LinkageError e) {
            plugin.warn(plugin.getName() + " 的 adventure 相关库似乎出现了依赖冲突问题，请参考以下链接进行解决");
            plugin.warn("https://plugins.mcio.dev/elopers/base/resolver-override");
            throw e;
        }
        try {
            AdventureItemStack.init(plugin);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 获取 Adventure 处理器
     */
    public static IAdventureHandler handler() {
        return handler;
    }

    /**
     * 设置 Adventure 处理器
     */
    public static void handler(IAdventureHandler handler) {
        AdventureUtil.handler = handler;
    }

    /**
     * 获取 adventure-platform-bukkit 的实例
     */
    public static BukkitAudiences adventure() {
        return handler.adventure();
    }

    /**
     * 获取 Bukkit CommandSender 在 adventure 的 Audience，如果有本地平台实现，优先使用本地平台实现
     */
    public static Audience of(CommandSender sender) {
        return handler.of(sender);
    }

    /**
     * @see AdventureUtil#of(CommandSender)
     */
    public static Audience of(Player player) {
        return handler.of(player);
    }

    /**
     * @see AdventureUtil#of(CommandSender)
     */
    public static Audience of(UUID player) {
        return handler.of(player);
    }

    /**
     * @see AdventureUtil#of(CommandSender)
     */
    public static Audience console() {
        return handler.console();
    }

    /**
     * 获取 MiniMessage 实例
     */
    public static MiniMessage miniMessage() {
        return handler.miniMessage();
    }

    /**
     * 将字符串通过 MiniMessage 转换为 Component
     */
    @NotNull
    public static Component miniMessage(@Nullable String s) {
        return handler.miniMessage(s);
    }

    /**
     * 将字符串通过 MiniMessage 转换为 Component
     */
    @NotNull
    public static Component miniMessage(@NotNull MiniMessage miniMessage, @Nullable String s) {
        return handler.miniMessage(miniMessage, s);
    }

    /**
     * 将 Component 通过 MiniMessage 转换为字符串
     */
    @NotNull
    public static String miniMessage(@Nullable Component component) {
        return handler.miniMessage(component);
    }

    /**
     * 将 Component 通过 MiniMessage 转换为字符串
     */
    @NotNull
    public static String miniMessage(@NotNull MiniMessage miniMessage, @Nullable Component component) {
        return handler.miniMessage(miniMessage, component);
    }

    /**
     * 将字符串列表通过 MiniMessage 转换为 Component 列表
     */
    @NotNull
    public static List<Component> miniMessage(List<String> list) {
        return handler.miniMessage(list);
    }

    /**
     * 将字符串列表通过 MiniMessage 转换为 Component 列表
     */
    @NotNull
    public static List<Component> miniMessage(MiniMessage miniMessage, List<String> list) {
        return handler.miniMessage(miniMessage, list);
    }

    /**
     * 将字符串列表通过 MiniMessage 转换为 Component，列表每一项均为一行
     */
    public static Component miniMessageLines(List<String> list) {
        return handler.miniMessageLines(list);
    }

    /**
     * 将字符串列表通过 MiniMessage 转换为 Component，列表每一项均为一行
     */
    public static Component miniMessageLines(MiniMessage miniMessage, List<String> list) {
        return handler.miniMessageLines(miniMessage, list);
    }

    /**
     * 将 Component 列表通过 MiniMessage 转换为字符串列表
     */
    @NotNull
    public static List<String> miniMessage_(List<Component> components) {
        return handler.miniMessage_(components);
    }

    /**
     * 将 Component 列表通过 MiniMessage 转换为字符串列表
     */
    @NotNull
    public static List<String> miniMessage_(MiniMessage miniMessage, List<Component> components) {
        return handler.miniMessage_(miniMessage, components);
    }

    /**
     * 移除文本组件中所有的 hover event 和 click event。
     */
    @Contract("null->null")
    public static Component removeEvents(Component component) {
        if (component == null) return null;
        List<Component> children = new ArrayList<>();
        for (Component child : component.children()) {
            children.add(removeEvents(child));
        }
        return component.hoverEvent(null).clickEvent(null).children(children);
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
    public static void sendTitle(Player player, Component title, Component subTitle, int fadeIn, int stay, int fadeOut) {
        handler.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
    }

    /**
     * 向玩家发送标题消息
     * @param player 玩家
     * @param miniMessage MiniMessage 实例
     * @param title 大标题
     * @param subTitle 副标题
     * @param fadeIn 淡入时间 (tick)
     * @param stay 保持时间 (tick)
     * @param fadeOut 淡出时间 (tick)
     */
    public static void sendTitle(Player player, MiniMessage miniMessage, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        handler.sendTitle(player, miniMessage, title, subTitle, fadeIn, stay, fadeOut);
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
        handler.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
    }

    /**
     * 重置玩家标题
     * @param player 玩家
     */
    public static void resetTitle(Player player) {
        handler.resetTitle(player);
    }

    /**
     * 清空玩家标题
     * @param player 玩家
     */
    public static void clearTitle(Player player) {
        handler.clearTitle(player);
    }

    /**
     * 向用户发送消息，支持 MiniMessage
     * @param sender 消息接收者
     * @param message 消息
     */
    public static void sendMessage(CommandSender sender, String message) {
        handler.sendMessage(sender, message);
    }

    /**
     * 向用户发送消息，支持 MiniMessage
     * @param sender 消息接收者
     * @param miniMessage 自定义 MiniMessage 实例
     * @param message 消息
     */
    public static void sendMessage(CommandSender sender, MiniMessage miniMessage, String message) {
        handler.sendMessage(sender, miniMessage, message);
    }

    /**
     * 向用户发送消息
     * @param sender 消息接收者
     * @param message 消息
     */
    public static void sendMessage(CommandSender sender, Component message) {
        handler.sendMessage(sender, message);
    }

    /**
     * 向玩家发送 ActionBar 消息，即物品栏上方的消息
     * @param player 玩家
     * @param message 消息
     */
    public static void sendActionBar(Player player, String message) {
        handler.sendActionBar(player, message);
    }

    /**
     * 向玩家发送 ActionBar 消息，即物品栏上方的消息
     * @param player 玩家
     * @param miniMessage 自定义 MiniMessage 实例
     * @param message 消息
     */
    public static void sendActionBar(Player player, MiniMessage miniMessage, String message) {
        handler.sendActionBar(player, miniMessage, message);
    }

    /**
     * 向玩家发送 ActionBar 消息
     * @param player 玩家
     * @param message 消息
     */
    public static void sendActionBar(Player player, Component message) {
        handler.sendActionBar(player, message);
    }

    /**
     * 将过时的 <code>&amp;</code>、<code>§</code> 样式代码转换为 MiniMessage 格式
     */
    public static String legacyToMiniMessage(String legacy) {
        return handler.legacyToMiniMessage(legacy);
    }
}
