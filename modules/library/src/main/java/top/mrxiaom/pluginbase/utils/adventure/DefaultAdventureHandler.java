package top.mrxiaom.pluginbase.utils.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAdventureHandler;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.adventure.audience.AudienceConsole;
import top.mrxiaom.pluginbase.utils.adventure.audience.AudiencePlayer;
import top.mrxiaom.pluginbase.utils.adventure.test.*;

import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class DefaultAdventureHandler implements IAdventureHandler, Listener {
    private final List<String> disabledTags = new ArrayList<>();
    private final Map<UUID, AudiencePlayer> players = new HashMap<>();
    protected ITagSerializer miniMessage;
    private final Supplier<ITagSerializer.Builder> defaultBuilder;
    @SuppressWarnings("unchecked")
    public DefaultAdventureHandler(BukkitPlugin plugin) {
        File file = plugin == null
                ? new File("pluginbase.yml")
                : new File(plugin.getDataFolder(), "pluginbase.yml");
        Supplier<ITagSerializer.Builder> sparrowBuilder = null;
        try {
            Class<?> sparrow = Class.forName("top.mrxiaom.pluginbase.utils.adventure.SparrowMiniMessage");
            Method method = sparrow.getDeclaredMethod("builderSupplier");
            sparrowBuilder = (Supplier<ITagSerializer.Builder>) method.invoke(null);
        } catch (Throwable ignored) {
        }
        if (sparrowBuilder != null) {
            if (file.exists()) {
                YamlConfiguration config = ConfigUtils.load(file);
                if (config.getBoolean("using-default-mini-message")) {
                    defaultBuilder = DefaultMiniMessage::builder;
                } else {
                    defaultBuilder = sparrowBuilder;
                }
            } else {
                defaultBuilder = sparrowBuilder;
            }
        } else {
            defaultBuilder = DefaultMiniMessage::builder;
        }
        Map<String, IAdventureTest> tagImplMap = new HashMap<>();
        tagImplMap.put("shadow", new TestShadow());
        tagImplMap.put("font", new TestFont());
        tagImplMap.put("gradient", new TestGradient());
        tagImplMap.put("head", new TestHead());
        tagImplMap.put("sprite", new TestSprite());
        disabledTags.add("pride");
        tagImplMap.forEach((tag, type) -> {
            try {
                type.test();
            } catch (Throwable e) {
                disabledTags.add(tag);
            }
        });
        miniMessage = builder().build();
        if (plugin != null) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    protected @NotNull ITagSerializer.Builder rawBuilder() {
        return defaultBuilder.get();
    }

    @Override
    public @NotNull ITagSerializer.Builder builder(boolean legacyProcessor) {
        ITagSerializer.Builder builder = rawBuilder();
        builder.editTags(it -> it.removeTags(disabledTags));
        if (legacyProcessor) {
            builder.preProcessor(this::legacyToMiniMessage);
        }
        builder.postProcessor(it -> it.decoration(TextDecoration.ITALIC, false));
        return builder;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        players.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public @NotNull Audience of(@NotNull CommandSender sender) {
        // Paper: 使用本地 adventure 实现
        if (sender instanceof Audience) {
            return (Audience) sender;
        }
        // Spigot: 使用转换为 BungeeCord Chat Components 发送的实现
        if (sender instanceof ConsoleCommandSender) {
            return AudienceConsole.INSTANCE;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            return CollectionUtils.getOrPut(players, uuid, () -> new AudiencePlayer(player));
        }
        return Audience.empty();
    }

    @Override
    public @NotNull ITagSerializer miniMessage() {
        return miniMessage;
    }

    @Override
    public @NotNull Component miniMessage(@NotNull ITagSerializer miniMessage, @Nullable String s) {
        if (s == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(s);
    }

    @Override
    public @NotNull String miniMessage(@NotNull ITagSerializer miniMessage, @Nullable Component component) {
        if (component == null) {
            return "";
        }
        return miniMessage.serialize(component);
    }

    @Override
    public @NotNull List<Component> miniMessage(ITagSerializer miniMessage, List<String> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<Component> components = new ArrayList<>();
        for (String s : list) {
            components.add(miniMessage(miniMessage, s));
        }
        return components;
    }

    @Override
    public @NotNull Component miniMessageLines(ITagSerializer miniMessage, List<String> list) {
        if (list == null || list.isEmpty()) return Component.empty();
        TextComponent.Builder text = Component.text();
        text.append(miniMessage(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            text.append(Component.newline());
            text.append(miniMessage(miniMessage, list.get(i)));
        }
        return text.build();
    }

    @Override
    public @NotNull List<String> miniMessage_(ITagSerializer miniMessage, List<Component> components) {
        if (components == null) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (Component component : components) {
            list.add(miniMessage(miniMessage, component));
        }
        return list;
    }

    @Override
    public void sendTitle(@NotNull Player player, @NotNull Component title, @NotNull Component subTitle, int fadeIn, int stay, int fadeOut) {
        of(player).showTitle(Title.title(
                title, subTitle, Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    @Override
    public void sendTitle(@NotNull Player player, @NotNull ITagSerializer miniMessage, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, miniMessage(miniMessage, title), miniMessage(miniMessage, subTitle), fadeIn, stay, fadeOut);
    }

    @Override
    public void resetTitle(@NotNull Player player) {
        of(player).resetTitle();
    }

    @Override
    public void clearTitle(@NotNull Player player) {
        of(player).clearTitle();
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull Component message) {
        of(sender).sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull ITagSerializer miniMessage, @NotNull String message) {
        sendMessage(sender, miniMessage(miniMessage, message));
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull Component message) {
        of(player).sendActionBar(message);
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull ITagSerializer miniMessage, @NotNull String message) {
        sendActionBar(player, miniMessage(message));
    }

    private static void appendColor(AtomicBoolean reset, StringBuilder builder, String color) {
        if (reset.compareAndSet(true, false)) {
            builder.append("<reset><!i>");
        }
        builder.append(color);
    }

    @Override
    public @NotNull String legacyToMiniMessage(@NotNull String legacy) {
        StringBuilder builder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        AtomicBoolean r = new AtomicBoolean(false);
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
                case '0': appendColor(r, builder, "<black>"); break;
                case '1': appendColor(r, builder, "<dark_blue>"); break;
                case '2': appendColor(r, builder, "<dark_green>"); break;
                case '3': appendColor(r, builder, "<dark_aqua>"); break;
                case '4': appendColor(r, builder, "<dark_red>"); break;
                case '5': appendColor(r, builder, "<dark_purple>"); break;
                case '6': appendColor(r, builder, "<gold>"); break;
                case '7': appendColor(r, builder, "<gray>"); break;
                case '8': appendColor(r, builder, "<dark_gray>"); break;
                case '9': appendColor(r, builder, "<blue>"); break;
                case 'a': appendColor(r, builder, "<green>"); break;
                case 'b': appendColor(r, builder, "<aqua>"); break;
                case 'c': appendColor(r, builder, "<red>"); break;
                case 'd': appendColor(r, builder, "<light_purple>"); break;
                case 'e': appendColor(r, builder, "<yellow>"); break;
                case 'f': appendColor(r, builder, "<white>"); break;
                case 'r': builder.append("<reset><!i>"); break;
                case 'l': builder.append("<b>"); r.set(true); break;
                case 'm': builder.append("<st>"); r.set(true); break;
                case 'o': builder.append("<i>"); r.set(true); break;
                case 'n': builder.append("<u>"); r.set(true); break;
                case 'k': builder.append("<obf>"); r.set(true); break;
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
                    if (r.compareAndSet(true, false)) {
                        builder.append("<reset><!i>");
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
                case '#': {
                    if (i + 6 >= chars.length) {
                        builder.append(chars[i]);
                        continue;
                    }
                    if (r.compareAndSet(true, false)) {
                        builder.append("<reset><!i>");
                    }
                    builder
                            .append("<#")
                            .append(chars,i+1, 6)
                            .append(">");
                    i += 5;
                    break;
                }
                default: {
                    builder.append(chars[i]);
                    if (chars[i+1] == chars[i]) { // && 转义为 &
                        i++;
                    }
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
