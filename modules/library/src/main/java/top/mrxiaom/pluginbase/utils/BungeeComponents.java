package top.mrxiaom.pluginbase.utils;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

import static top.mrxiaom.pluginbase.utils.CollectionUtils.split;

public class BungeeComponents {
    private static final Pattern translatePattern = Pattern.compile("<translate:(.*?)>");
    public static void parseAndSend(CommandSender sender, String message) {
        TextComponent builder = new TextComponent("");
        split(translatePattern, message, regexResult -> {
            if (!regexResult.isMatched) {
                builder.addExtra(new TextComponent(regexResult.text));
            } else {
                TranslatableComponent translatable = new TranslatableComponent(regexResult.result.group(1));
                builder.addExtra(translatable);
            }
        });
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(builder);
            return;
        }
        sender.spigot().sendMessage(builder);
    }
}
