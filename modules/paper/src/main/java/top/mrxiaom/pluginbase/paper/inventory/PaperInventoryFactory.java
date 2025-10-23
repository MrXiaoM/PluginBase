package top.mrxiaom.pluginbase.paper.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;

import java.lang.reflect.Field;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.legacyToMiniMessage;

public class PaperInventoryFactory implements InventoryFactory {
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private static Field resolversField;
    public PaperInventoryFactory() {
        miniMessage = MiniMessage.builder()
                .editTags(it -> remove(it, "pride"))
                .preProcessor(AdventureUtil::legacyToMiniMessage)
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
    }

    @SuppressWarnings({"unchecked"})
    private static void remove(TagResolver.Builder builder, String... tags) {
        try {
            if (resolversField == null) {
                resolversField = builder.getClass().getDeclaredField("resolvers");
                resolversField.setAccessible(true);
            }
            List<TagResolver> list = (List<TagResolver>) resolversField.get(builder);
            list.removeIf(it -> {
                for (String tag : tags) {
                    if (it.has(tag)) return true;
                }
                return false;
            });
        } catch (Throwable ignored) {
        }
    }

    public Component miniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(text);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Inventory create(InventoryHolder owner, int size, String title) {
        try {
            Component parsed = miniMessage(title);
            return Bukkit.createInventory(owner, size, parsed);
        } catch (LinkageError e) { // 1.16 以下的旧版本 Paper 服务端不支持这个接口
            Component parsed = AdventureUtil.miniMessage(title);
            return Bukkit.createInventory(owner, size, legacy.serialize(parsed));
        }
    }

    public static boolean test() {
        try {
            Bukkit.class.getDeclaredMethod("createInventory", InventoryHolder.class, InventoryType.class, Component.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
