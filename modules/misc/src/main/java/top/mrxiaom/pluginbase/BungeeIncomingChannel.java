package top.mrxiaom.pluginbase;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AbstractPluginHolder;
import top.mrxiaom.pluginbase.utils.Bytes;

public class BungeeIncomingChannel implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = Bytes.newDataInput(message);
        String subChannel = in.readUTF();
        short len = in.readShort();
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        AbstractPluginHolder.receiveFromBungee(subChannel, bytes);
    }
}
