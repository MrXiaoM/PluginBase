package top.mrxiaom.pluginbase.utils.adventure.test;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public class TestFont implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Component.empty().style().font(Key.key("default"));
    }
}
