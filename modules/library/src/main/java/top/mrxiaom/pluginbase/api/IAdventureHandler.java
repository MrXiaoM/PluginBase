package top.mrxiaom.pluginbase.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;
import top.mrxiaom.pluginbase.utils.adventure.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Objects;

public interface IAdventureHandler {
    @NotNull
    default ITagSerializer.Builder builder() {
        return builder(true);
    }
    @NotNull
    ITagSerializer.Builder builder(boolean legacyProcessor);
    @NotNull
    @ApiStatus.Experimental
    Audience of(@NotNull CommandSender sender);
    @NotNull
    @ApiStatus.Experimental
    default Audience of(@NotNull Player player) {
        return of((CommandSender) player);
    }
    @NotNull
    @ApiStatus.Experimental
    default Audience console() {
        return of(Bukkit.getConsoleSender());
    }
    @NotNull
    ITagSerializer miniMessage();

    @NotNull
    Component miniMessage(@NotNull ITagSerializer miniMessage, @Nullable String s);
    @NotNull
    default Component miniMessage(@Nullable String s) {
        return miniMessage(miniMessage(), s);
    }

    @NotNull
    String miniMessage(@NotNull ITagSerializer miniMessage, @Nullable Component component);
    @NotNull
    default String miniMessage(@Nullable Component component) {
        return miniMessage(miniMessage(), component);
    }

    @NotNull
    List<Component> miniMessage(ITagSerializer miniMessage, List<String> list);
    @NotNull
    default List<Component> miniMessage(List<String> list) {
        return miniMessage(miniMessage(), list);
    }
    @NotNull
    Component miniMessageLines(ITagSerializer miniMessage, List<String> list);
    @NotNull
    default Component miniMessageLines(List<String> list) {
        return miniMessageLines(miniMessage(), list);
    }

    @NotNull
    List<String> miniMessage_(ITagSerializer miniMessage, List<Component> components);
    @NotNull
    default List<String> miniMessage_(List<Component> components) {
        return miniMessage_(miniMessage(), components);
    }

    @NotNull
    default String plain(@NotNull Component component) {
        Component input = Objects.requireNonNull(component, "component");
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(input, sb::append);
        return sb.toString();
    }

    @NotNull
    default String legacyAmpersand(@NotNull Component component) {
        Component input = Objects.requireNonNull(component, "component");
        return LegacyComponentSerializer.legacyAmpersand().serialize(input);
    }

    @NotNull
    default String legacySection(@NotNull Component component) {
        Component input = Objects.requireNonNull(component, "component");
        return LegacyComponentSerializer.legacySection().serialize(input);
    }

    void sendTitle(@NotNull Player player, @NotNull Component title, @NotNull Component subTitle, int fadeIn, int stay, int fadeOut);
    void sendTitle(@NotNull Player player, @NotNull ITagSerializer miniMessage, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut);
    default void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, miniMessage(), title, subTitle, fadeIn, stay, fadeOut);
    }
    void resetTitle(@NotNull Player player);
    void clearTitle(@NotNull Player player);

    void sendMessage(@NotNull CommandSender sender, @NotNull Component message);
    void sendMessage(@NotNull CommandSender sender, @NotNull ITagSerializer miniMessage, @NotNull String message);
    default void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, miniMessage(), message);
    }

    void sendActionBar(@NotNull Player player, @NotNull Component message);
    void sendActionBar(@NotNull Player player, @NotNull ITagSerializer miniMessage, @NotNull String message);
    default void sendActionBar(@NotNull Player player, @NotNull String message) {
        sendActionBar(player, miniMessage(), message);
    }

    @NotNull
    String legacyToMiniMessage(@NotNull String legacy);

}
