package top.mrxiaom.pluginbase.benchmark;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 防止性能基准对语义不同的输出进行直接排名。
 */
public class MiniMessageBenchmarkEquivalenceTest {
    @Test
    public void commonDeserializeSamplesAreEquivalent() {
        MiniMessageBenchmarkFixtures.assertEquivalent();
    }

    @Test
    public void serializedComponentsRemainInteroperableByVisibleText() {
        final ITagSerializer sparrow = MiniMessageBenchmarkFixtures.sparrow();
        final ITagSerializer defaultMiniMessage = MiniMessageBenchmarkFixtures.defaultMiniMessage();

        assertCrossReadable("基础组件", MiniMessageBenchmarkFixtures.serializationBasicComponent(), sparrow, defaultMiniMessage);
        assertCrossReadable("样式组件", MiniMessageBenchmarkFixtures.serializationStyledComponent(), sparrow, defaultMiniMessage);
        assertCrossReadable("交互组件", MiniMessageBenchmarkFixtures.serializationInteractiveComponent(), sparrow, defaultMiniMessage);

        final Component sparrowGradient = MiniMessageBenchmarkFixtures.serializationGradientComponent(sparrow);
        final Component defaultGradient = MiniMessageBenchmarkFixtures.serializationGradientComponent(defaultMiniMessage);
        assertEquals(
                MiniMessageBenchmarkFixtures.visibleText(defaultGradient),
                MiniMessageBenchmarkFixtures.visibleText(sparrowGradient),
                "渐变反序列化必须产生相同的可见文本"
        );
        assertCrossReadable("渐变组件", sparrowGradient, sparrow, defaultMiniMessage);
    }

    private static void assertCrossReadable(
            final String name,
            final Component component,
            final ITagSerializer sparrow,
            final ITagSerializer defaultMiniMessage
    ) {
        final String sparrowSerialized = sparrow.serialize(component);
        final String defaultSerialized = defaultMiniMessage.serialize(component);

        final String expectedVisibleText = MiniMessageBenchmarkFixtures.visibleText(component);
        assertEquals(expectedVisibleText, MiniMessageBenchmarkFixtures.visibleText(sparrow.deserialize(sparrowSerialized)), name + " 必须可由 Sparrow 往返解析");
        assertEquals(expectedVisibleText, MiniMessageBenchmarkFixtures.visibleText(defaultMiniMessage.deserialize(defaultSerialized)), name + " 必须可由 Default 往返解析");
        assertEquals(expectedVisibleText, MiniMessageBenchmarkFixtures.visibleText(defaultMiniMessage.deserialize(sparrowSerialized)), name + " 的 Sparrow 序列化结果必须可由 Default 解析");
        assertEquals(expectedVisibleText, MiniMessageBenchmarkFixtures.visibleText(sparrow.deserialize(defaultSerialized)), name + " 的 Default 序列化结果必须可由 Sparrow 解析");
    }
}
