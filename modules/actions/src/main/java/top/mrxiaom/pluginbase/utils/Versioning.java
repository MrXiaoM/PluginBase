package top.mrxiaom.pluginbase.utils;

public class Versioning {
    private static String minecraftVersion;

    public static String getMinecraftVersion() {
        if (minecraftVersion != null) {
            return minecraftVersion;
        }
        try {
            // 26.1+ mojmap NMS
            return minecraftVersion = net.minecraft.SharedConstants.getCurrentVersion().name();
        } catch (LinkageError ignored) {
        }
        try {
            // Paper
            return minecraftVersion = org.bukkit.Bukkit.getServer().getMinecraftVersion();
        } catch (LinkageError ignored) {
        }
        // Spigot / Legacy Paper
        return minecraftVersion = org.bukkit.Bukkit.getServer().getBukkitVersion().split("-")[0];
    }
}
