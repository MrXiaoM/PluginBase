package top.mrxiaom.pluginbase.utils.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class AudienceConsole implements Audience {
    public static final Audience INSTANCE = new AudienceConsole();
    private static final BungeeComponentSerializer bungee = BungeeComponentSerializer.get();
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private AudienceConsole() {}

    @Override
    public void sendMessage(@NotNull Component message) {
        BaseComponent[] components = bungee.serialize(message);
        console.spigot().sendMessage(components);
    }
}
