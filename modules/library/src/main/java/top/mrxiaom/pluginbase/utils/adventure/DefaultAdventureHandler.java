package top.mrxiaom.pluginbase.utils.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAdventureHandler;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DefaultAdventureHandler implements IAdventureHandler {
    private static Field resolversField;
    protected BukkitAudiences adventure;
    protected MiniMessage miniMessage;
    public DefaultAdventureHandler(BukkitPlugin plugin) {
        adventure = BukkitAudiences.builder(plugin).build();
        miniMessage = builder().build();
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    public static void remove(TagResolver.Builder builder, String... tags) {
        try {
            if (resolversField == null) {
                resolversField = builder.getClass().getDeclaredField("resolvers");
                resolversField.setAccessible(true);
            }
            List<TagResolver> list = (List<TagResolver>) resolversField.get(builder);
            list.removeIf(it -> {
                for (String tag : tags) {
                    if (it.has(tag)) return true;
                }
                return false;
            });
        } catch (Throwable ignored) {
        }
    }

    @Override
    public @NotNull MiniMessage.Builder builder() {
        return MiniMessage.builder()
                .editTags(it -> remove(it, "pride"))
                .preProcessor(AdventureUtil::legacyToMiniMessage)
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false));
    }

    @Override
    public @NotNull BukkitAudiences adventure() {
        return adventure;
    }

    @Override
    public @NotNull Audience of(@NotNull CommandSender sender) {
        if (sender instanceof Audience) {
            return (Audience) sender;
        }
        return adventure.sender(sender);
    }

    @Override
    public @NotNull MiniMessage miniMessage() {
        return miniMessage;
    }

    @Override
    public @NotNull Component miniMessage(@NotNull MiniMessage miniMessage, @Nullable String s) {
        if (s == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(s);
    }

    @Override
    public @NotNull String miniMessage(@NotNull MiniMessage miniMessage, @Nullable Component component) {
        if (component == null) {
            return "";
        }
        return miniMessage.serialize(component);
    }

    @Override
    public @NotNull List<Component> miniMessage(MiniMessage miniMessage, List<String> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<Component> components = new ArrayList<>();
        for (String s : list) {
            components.add(miniMessage(miniMessage, s));
        }
        return components;
    }

    @Override
    public @NotNull Component miniMessageLines(MiniMessage miniMessage, List<String> list) {
        if (list == null || list.isEmpty()) return Component.empty();
        TextComponent.Builder text = Component.text();
        text.append(miniMessage(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            text.appendNewline();
            text.append(miniMessage(miniMessage, list.get(i)));
        }
        return text.build();
    }

    @Override
    public @NotNull List<String> miniMessage_(MiniMessage miniMessage, List<Component> components) {
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
    public void sendTitle(@NotNull Player player, @NotNull MiniMessage miniMessage, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut) {
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
    public void sendMessage(@NotNull CommandSender sender, @NotNull MiniMessage miniMessage, @NotNull String message) {
        sendMessage(sender, miniMessage(miniMessage, message));
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull Component message) {
        of(player).sendActionBar(message);
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull MiniMessage miniMessage, @NotNull String message) {
        sendActionBar(player, miniMessage(message));
    }

    @Override
    public @NotNull String legacyToMiniMessage(@NotNull String legacy) {
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
                case '#': {
                    if (i + 6 >= chars.length) {
                        builder.append(chars[i]);
                        continue;
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
