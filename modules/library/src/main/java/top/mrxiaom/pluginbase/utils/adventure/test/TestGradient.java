package top.mrxiaom.pluginbase.utils.adventure.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class TestGradient implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Component.empty().style().color(TextColor.color(255, 255, 255));
    }
}
