package top.mrxiaom.pluginbase.utils.adventure.test;

public class TestSprite implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Class.forName("net.kyori.adventure.text.object.ObjectContents");
        Class.forName("net.kyori.adventure.text.object.SpriteObjectContents");
    }
}
