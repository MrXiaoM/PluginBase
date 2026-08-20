package top.mrxiaom.pluginbase.utils.adventure.test;

public class TestShadow implements IAdventureTest {
    @Override
    public void test() throws Throwable {
        Class.forName("net.kyori.adventure.util.ARGBLike");
        Class.forName("net.kyori.adventure.text.format.ShadowColor");
    }
}
