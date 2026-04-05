package top.mrxiaom.pluginbase.actions;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;

import java.util.List;

public class ActionSound implements IAction {
    public static final IActionProvider PROVIDER = input -> {
        if (input instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) input;
            if ("sound".equals(section.getString("type"))) {
                String sound = section.getString("sound");
                if (sound != null) {
                    float volume = ConfigUtils.getFloat(section, "volume", 1.0f);
                    float pitch = ConfigUtils.getFloat(section, "pitch", 1.0f);
                    return new ActionSound(sound, volume, pitch);
                }
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[sound]")) {
                String[] split = s.split(",");
                String sound = split[0];
                float volume = split.length > 1 ? Util.parseFloat(split[1]).orElse(1.0f) : 1.0f;
                float pitch = split.length > 2 ? Util.parseFloat(split[2]).orElse(1.0f) : 1.0f;
                return new ActionSound(sound, volume, pitch);
            }
        }
        return null;
    };
    private final String sound;
    private final float volume, pitch;
    public ActionSound(String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player != null) {
            Location location = player.getLocation();
            String sound = PAPI.setPlaceholders(player, Pair.replace(this.sound, replacements));
            player.playSound(location, sound, volume, pitch);
        }
    }
}
