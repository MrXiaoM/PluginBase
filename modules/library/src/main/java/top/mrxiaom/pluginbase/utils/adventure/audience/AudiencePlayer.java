package top.mrxiaom.pluginbase.utils.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AudiencePlayer implements Audience {
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private static final BungeeComponentSerializer bungee = BungeeComponentSerializer.get();
    private final Player player;
    public AudiencePlayer(Player player) {
        this.player = player;
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        BaseComponent[] components = bungee.serialize(message);
        player.spigot().sendMessage(components);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        BaseComponent[] components = bungee.serialize(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void showTitle(@NotNull Title title) {
        String strTitle = legacy.serialize(title.title());
        String strSubtitle = legacy.serialize(title.subtitle());
        Title.Times times = title.times();
        if (times != null) {
            int fadeIn = (int) (times.fadeIn().toMillis() / 50.0);
            int stay = (int) (times.stay().toMillis() / 50.0);
            int fadeOut = (int) (times.fadeOut().toMillis() / 50.0);
            player.sendTitle(strTitle, strSubtitle, fadeIn, stay, fadeOut);
        } else {
            player.sendTitle(strTitle, strSubtitle);
        }
    }

    @Override
    public void clearTitle() {
        player.resetTitle();
    }

    @Override
    public void resetTitle() {
        player.resetTitle();
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        playSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        Location loc = player.getLocation();
        player.playSound(loc, sound.name().asString(), sound.volume(), sound.pitch());
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        Location loc = new Location(player.getWorld(), x, y, z);
        player.playSound(loc, sound.name().asString(), sound.volume(), sound.pitch());
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        player.stopSound(sound.name().asString());
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        SoundCategory category = from(stop.source());
        if (category != null) {
            Key sound = stop.sound();
            if (sound != null) {
                player.stopSound(sound.asString(), category);
            } else {
                player.stopSound(category);
            }
        } else {
            Key sound = stop.sound();
            if (sound != null) {
                player.stopSound(sound.asString());
            } else {
                player.stopAllSounds();
            }
        }
    }

    private static SoundCategory from(Sound.Source source) {
        if (source == null) return null;
        String name = source.name().toUpperCase();
        for (SoundCategory category : SoundCategory.values()) {
            if (category.name().toUpperCase().startsWith(name)) {
                return category;
            }
        }
        return null;
    }
}
