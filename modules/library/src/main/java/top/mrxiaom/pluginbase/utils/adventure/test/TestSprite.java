package top.mrxiaom.pluginbase.utils.adventure.test;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

public class TestSprite implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Component.object().contents(ObjectContents.sprite(Key.key("blocks"), Key.key("block/stone")));
    }
}
