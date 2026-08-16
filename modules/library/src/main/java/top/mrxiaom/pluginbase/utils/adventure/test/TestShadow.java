package top.mrxiaom.pluginbase.utils.adventure.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;

public class TestShadow implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Component.empty().style().shadowColor(ShadowColor.none());
    }
}
