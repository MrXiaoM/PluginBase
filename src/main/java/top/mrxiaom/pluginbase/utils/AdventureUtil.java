package top.mrxiaom.pluginbase.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.BukkitPlugin;

import java.time.Duration;
import java.util.UUID;

public class AdventureUtil {
    private static BukkitAudiences adventure;
    private static MiniMessage miniMessage;

    protected static void init(BukkitPlugin plugin) {
        adventure = BukkitAudiences.builder(plugin).build();
        miniMessage = MiniMessage.builder()
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
    }

    public static BukkitAudiences adventure() {
        return adventure;
    }

    public static Audience of(Player player) {
        return adventure.player(player);
    }

    public static Audience of(UUID player) {
        return adventure.player(player);
    }

    public static Audience console() {
        return adventure.sender(Bukkit.getConsoleSender());
    }

    public static MiniMessage miniMessage() {
        return miniMessage;
    }

    public static Component miniMessage(String s) {
        return s == null
                ? Component.empty()
                : miniMessage.deserialize(legacyToMiniMessage(s));
    }

    public static String miniMessage(Component component) {
        return component == null
                ? ""
                : miniMessage.serialize(component);
    }

    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        adventure.player(player).showTitle(Title.title(
                miniMessage(title), miniMessage(subTitle), Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    public static void resetTitle(Player player) {
        adventure.player(player).resetTitle();
    }

    public static void clearTitle(Player player) {
        adventure.player(player).clearTitle();
    }

    public static void sendMessage(CommandSender sender, String message) {
        adventure.sender(sender).sendMessage(miniMessage(message));
    }

    public static void sendActionBar(Player player, String actionBar) {
        adventure.player(player).sendActionBar(miniMessage(actionBar));
    }

    public static String legacyToMiniMessage(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                stringBuilder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                stringBuilder.append(chars[i]);
                continue;
            }
            switch (chars[i+1]) {
                case '0': stringBuilder.append("<black>"); break;
                case '1': stringBuilder.append("<dark_blue>"); break;
                case '2': stringBuilder.append("<dark_green>"); break;
                case '3': stringBuilder.append("<dark_aqua>"); break;
                case '4': stringBuilder.append("<dark_red>"); break;
                case '5': stringBuilder.append("<dark_purple>"); break;
                case '6': stringBuilder.append("<gold>"); break;
                case '7': stringBuilder.append("<gray>"); break;
                case '8': stringBuilder.append("<dark_gray>"); break;
                case '9': stringBuilder.append("<blue>"); break;
                case 'a': stringBuilder.append("<green>"); break;
                case 'b': stringBuilder.append("<aqua>"); break;
                case 'c': stringBuilder.append("<red>"); break;
                case 'd': stringBuilder.append("<light_purple>"); break;
                case 'e': stringBuilder.append("<yellow>"); break;
                case 'f': stringBuilder.append("<white>"); break;
                case 'r': stringBuilder.append("<reset><!i>"); break;
                case 'l': stringBuilder.append("<b>"); break;
                case 'm': stringBuilder.append("<st>"); break;
                case 'o': stringBuilder.append("<i>"); break;
                case 'n': stringBuilder.append("<u>"); break;
                case 'k': stringBuilder.append("<obf>"); break;
                case 'x': {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        stringBuilder.append(chars[i]);
                        continue;
                    }
                    stringBuilder
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
                    stringBuilder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return stringBuilder.toString();
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }
}
