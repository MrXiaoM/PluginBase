package top.mrxiaom.pluginbase;

import org.junit.jupiter.api.Test;
import top.mrxiaom.pluginbase.api.IAdventureHandler;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;
import top.mrxiaom.pluginbase.utils.adventure.DefaultAdventureHandler;

/**
 * 使用 Adventure 4.25.0 编译，并通过替换 testRuntimeOnly 依赖观察各运行时版本的兼容性。
 *
 * <p>不比较原始输入和序列化输出：MiniMessage 会对等价组件进行规范化。</p>
 */
public class MiniMessageTest {
    private static final IAdventureHandler handler = new DefaultAdventureHandler(null);
    @Test
    public void basicFormatting() {
        test("Basic text, escape, colors and decorations", "A normal text <red><bold>Red bold text</bold></red> \\<Not a tag>");
    }

    @Test
    public void colorTransitions() {
        test("Gradient, rainbow and transition", "<gradient:#ff0000:#00ff00>A gradient text</gradient> <rainbow:!>Rainbow Text</rainbow> <transition:#00ffff:#ff00ff:0.25>Transition Text</transition>");
    }

    @Test
    public void interactiveEvents() {
        test("Click event, hover event and insertion", "<click:run_command:'/say hello'><hover:show_text:'<yellow>Something to show</yellow>'><insertion:inserted>Interaction text</insertion></hover></click>");
    }

    @Test
    public void componentReferences() {
        test("Translatable, keybinding, selector, scoreboard and NBT", "<lang:minecraft.item.apple> <key:key.jump> <selector:@a> <score:Player:objective> <nbt:entity:@s:Health>");
    }

    @Test
    public void modernComponents() {
        test("Font, shadow, sprite and head", "<font:minecraft:default><shadow:#11223380>Font and shadow</shadow></font> <sprite:minecraft:gui/icons> <head:Mirai>");
    }

    private static void test(String name, String input) {
        ITagSerializer miniMessage = handler.miniMessage();
        String output = miniMessage.serialize(miniMessage.deserialize(input));

        System.out.println("========== " + name + " ==========");
        System.out.println("Serializer: " + miniMessage.getClass().getName());
        System.out.println("Input : " + input);
        System.out.println("Output: " + output);
    }
}
