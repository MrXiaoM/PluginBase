package top.mrxiaom.pluginbase.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
public class PAPI {
    private static boolean isEnabled = false;
    protected static void init() {
        isEnabled = Util.isPresent("me.clip.placeholderapi.PlaceholderAPI");
    }

    public static String setPlaceholders(OfflinePlayer player, String s) {
        if (!isEnabled) return s.replace("%player_name%", String.valueOf(player.getName()));
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static String setPlaceholders(Player player, String s) {
        if (!isEnabled) return s.replace("%player_name%", player.getName());
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static List<String> setPlaceholders(OfflinePlayer player, List<String> list) {
        if (!isEnabled) {
            List<String> result = new ArrayList<>();
            String playerName = String.valueOf(player.getName());
            for (String s : list) {
                result.add(s.replace("%player_name%", playerName));
            }
            return result;
        }
        return PlaceholderAPI.setPlaceholders(player, list);
    }
    public static List<String> setPlaceholders(Player player, List<String> list) {
        if (!isEnabled) {
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
