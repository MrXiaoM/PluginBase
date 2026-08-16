package top.mrxiaom.pluginbase.utils.adventure.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

public class TestHead implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Component.object().contents(ObjectContents.playerHead("Steve"));
    }
}
