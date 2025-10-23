package top.mrxiaom.pluginbase.utils.depend;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
public class PAPI {
    private static boolean isEnabled = false;
    public static void init() {
        isEnabled = Util.isPresent("me.clip.placeholderapi.PlaceholderAPI");
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static @NotNull String setPlaceholders(@Nullable OfflinePlayer player, @NotNull String s) {
        if (!isEnabled) {
            if (player == null) return s;
            return s.replace("%player_name%", String.valueOf(player.getName()));
        }
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static @NotNull String setPlaceholders(@Nullable Player player, @NotNull String s) {
        if (!isEnabled) {
            if (player == null) return s;
            return s.replace("%player_name%", player.getName());
        }
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static @NotNull List<String> setPlaceholders(@Nullable OfflinePlayer player, @NotNull List<String> list) {
        if (!isEnabled) {
            if (player == null) return new ArrayList<>(list);
            List<String> result = new ArrayList<>();
            String playerName = String.valueOf(player.getName());
            for (String s : list) {
                result.add(s.replace("%player_name%", playerName));
            }
            return result;
        }
        return PlaceholderAPI.setPlaceholders(player, list);
    }
    public static @NotNull List<String> setPlaceholders(@Nullable Player player, @NotNull List<String> list) {
        if (!isEnabled) {
            if (player == null) return new ArrayList<>(list);
            List<String> result = new ArrayList<>();
            String playerName = player.getName();
            for (String s : list) {
                result.add(s.replace("%player_name%", playerName));
            }
            return result;
        }
        return PlaceholderAPI.setPlaceholders(player, list);
    }
}
