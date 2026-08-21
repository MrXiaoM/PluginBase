package top.mrxiaom.pluginbase;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;
import top.mrxiaom.pluginbase.utils.adventure.serializer.legacy.LegacyComponentSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LegacyComponentTest {
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

    @Test
    public void convertToComponent() {
        String input = "§aTest";
        TextComponent component = serializer.deserialize(input);

        assertEquals("Test", component.content());
        assertEquals("green", String.valueOf(component.color()));
    }

    @Test
    public void convertToLegacy() {
        Component input = Component.text("Test").color(NamedTextColor.GREEN);
        String legacyText = serializer.serialize(input);

        assertEquals("§aTest", legacyText);
    }
}
