package top.mrxiaom.pluginbase.utils.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.adventure.serializer.BungeeComponentSerializer;
import top.mrxiaom.pluginbase.utils.adventure.serializer.legacy.LegacyComponentSerializer;

public class AudienceConsole implements Audience {
    private static boolean SUPPORT_BUNGEE = true;
    public static final Audience INSTANCE = new AudienceConsole();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private AudienceConsole() {}

    @Override
    public void sendMessage(@NotNull Component message) {
        if (SUPPORT_BUNGEE) {
            try {
                BaseComponent components = BungeeComponentSerializer.serialize(message);
                console.spigot().sendMessage(components);
                return;
            } catch (LinkageError e) {
                SUPPORT_BUNGEE = false;
            }
        }
        console.sendMessage(legacy.serialize(message));
    }
}
