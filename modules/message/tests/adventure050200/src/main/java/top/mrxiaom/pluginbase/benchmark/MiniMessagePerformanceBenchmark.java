package top.mrxiaom.pluginbase.benchmark;

import net.kyori.adventure.text.Component;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;

import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * 比较 SparrowMiniMessage 与 Adventure 原生 DefaultMiniMessage 的 JMH 基准。
 *
 * <p>共同反序列化场景由 {@link MiniMessageBenchmarkFixtures#assertEquivalent()} 验证。
 * 渐变因两端内部 Component 图不同而单独标注为实现策略场景；解析器机制场景直接调用
 * 两端原生 API，测量动态标签缓存和大型静态解析器集的查找成本。</p>
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class MiniMessagePerformanceBenchmark {
    @Benchmark
    public void sparrowDeserialize(final DeserializeState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrow.deserialize(state.input));
    }

    @Benchmark
    public void defaultDeserialize(final DeserializeState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultMiniMessage.deserialize(state.input));
    }

    @Benchmark
    public void sparrowSerializeBasic(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrow.serialize(state.basic));
    }

    @Benchmark
    public void defaultSerializeBasic(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultMiniMessage.serialize(state.basic));
    }

    @Benchmark
    public void sparrowSerializeStyled(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrow.serialize(state.styled));
    }

    @Benchmark
    public void defaultSerializeStyled(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultMiniMessage.serialize(state.styled));
    }

    @Benchmark
    public void sparrowSerializeInteractive(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrow.serialize(state.interactive));
    }

    @Benchmark
    public void defaultSerializeInteractive(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultMiniMessage.serialize(state.interactive));
    }

    @Benchmark
    public void sparrowSerializeDeep(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrow.serialize(state.deep));
    }

    @Benchmark
    public void defaultSerializeDeep(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultMiniMessage.serialize(state.deep));
    }

    /** 渐变的组件树布局不同，故只比较各实现处理自身解析结果的成本。 */
    @Benchmark
    public void sparrowSerializeGradient(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrow.serialize(state.sparrowGradient));
    }

    /** 渐变的组件树布局不同，故只比较各实现处理自身解析结果的成本。 */
    @Benchmark
    public void defaultSerializeGradient(final SerializationState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultMiniMessage.serialize(state.defaultGradient));
    }

    /** 每次创建缓存解析器，测量动态无参数标签的首次解析成本。 */
    @Benchmark
    public void sparrowDynamicTagCold(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(sparrowCachingParser().deserialize(MiniMessageBenchmarkFixtures.DYNAMIC_HIT));
    }

    /** 每个 fork 复用同一缓存解析器，测量已缓存动态标签的稳定命中成本。 */
    @Benchmark
    public void sparrowDynamicTagHot(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrowDynamicHot.deserialize(MiniMessageBenchmarkFixtures.DYNAMIC_HIT));
    }

    /** 缓存同时保存 null 解析结果；该场景测量未知标签的重复查询。 */
    @Benchmark
    public void sparrowDynamicTagMissHot(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrowDynamicMissHot.deserialize(MiniMessageBenchmarkFixtures.DYNAMIC_MISS));
    }

    @Benchmark
    public void defaultDynamicTagCold(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(defaultCachingParser().deserialize(MiniMessageBenchmarkFixtures.DYNAMIC_HIT));
    }

    @Benchmark
    public void defaultDynamicTagHot(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultDynamicHot.deserialize(MiniMessageBenchmarkFixtures.DYNAMIC_HIT));
    }

    @Benchmark
    public void defaultDynamicTagMissHot(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultDynamicMissHot.deserialize(MiniMessageBenchmarkFixtures.DYNAMIC_MISS));
    }

    /** 16 个独立静态解析器触发 Sparrow 的构建期哈希分发表。 */
    @Benchmark
    public void sparrowLargeStaticResolver(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(state.sparrowLargeStatic.deserialize(MiniMessageBenchmarkFixtures.LARGE_STATIC));
    }

    @Benchmark
    public void defaultLargeStaticResolver(final ResolverState state, final Blackhole blackhole) {
        blackhole.consume(state.defaultLargeStatic.deserialize(MiniMessageBenchmarkFixtures.LARGE_STATIC));
    }

    /** 测量公共包装层的完整构建过程，包括移除标签、注册标签和处理器。 */
    @Benchmark
    public void sparrowBuilderConfiguration(final Blackhole blackhole) {
        blackhole.consume(MiniMessageBenchmarkFixtures.sparrow());
    }

    @Benchmark
    public void defaultBuilderConfiguration(final Blackhole blackhole) {
        blackhole.consume(MiniMessageBenchmarkFixtures.defaultMiniMessage());
    }

    @State(Scope.Benchmark)
    public static class DeserializeState {
        @Param({"basic", "styled", "interactive", "custom", "gradient"})
        public String scenario;
        private ITagSerializer sparrow;
        private ITagSerializer defaultMiniMessage;
        private String input;

        @Setup
        public void setUp() {
            MiniMessageBenchmarkFixtures.assertEquivalent();
            this.sparrow = MiniMessageBenchmarkFixtures.sparrow();
            this.defaultMiniMessage = MiniMessageBenchmarkFixtures.defaultMiniMessage();
            this.input = MiniMessageBenchmarkFixtures.sample(this.scenario);
        }
    }

    @State(Scope.Benchmark)
    public static class SerializationState {
        private ITagSerializer sparrow;
        private ITagSerializer defaultMiniMessage;
        private Component basic;
        private Component styled;
        private Component interactive;
        private Component deep;
        private Component sparrowGradient;
        private Component defaultGradient;

        @Setup
        public void setUp() {
            this.sparrow = MiniMessageBenchmarkFixtures.sparrow();
            this.defaultMiniMessage = MiniMessageBenchmarkFixtures.defaultMiniMessage();
            this.basic = MiniMessageBenchmarkFixtures.serializationBasicComponent();
            this.styled = MiniMessageBenchmarkFixtures.serializationStyledComponent();
            this.interactive = MiniMessageBenchmarkFixtures.serializationInteractiveComponent();
            this.deep = MiniMessageBenchmarkFixtures.serializationDeepComponent();
            this.sparrowGradient = MiniMessageBenchmarkFixtures.serializationGradientComponent(this.sparrow);
            this.defaultGradient = MiniMessageBenchmarkFixtures.serializationGradientComponent(this.defaultMiniMessage);
        }
    }

    @State(Scope.Benchmark)
    public static class ResolverState {
        private top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage sparrowDynamicHot;
        private top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage sparrowDynamicMissHot;
        private net.kyori.adventure.text.minimessage.MiniMessage defaultDynamicHot;
        private net.kyori.adventure.text.minimessage.MiniMessage defaultDynamicMissHot;
        private top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage sparrowLargeStatic;
        private net.kyori.adventure.text.minimessage.MiniMessage defaultLargeStatic;

        @Setup
        public void setUp() {
            this.sparrowDynamicHot = sparrowCachingParser();
            this.sparrowDynamicMissHot = sparrowCachingParser();
            this.defaultDynamicHot = defaultCachingParser();
            this.defaultDynamicMissHot = defaultCachingParser();
            this.sparrowLargeStatic = sparrowLargeStaticParser();
            this.defaultLargeStatic = defaultLargeStaticParser();
        }
    }

    private static top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage sparrowCachingParser() {
        final top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver.WithoutArguments dynamic =
                MiniMessagePerformanceBenchmark::dynamicSparrowTag;
        return top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage.builder()
                .tags(top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver.caching(dynamic))
                .postProcessor(UnaryOperator.identity())
                .build();
    }

    private static Tag dynamicSparrowTag(final String name) {
        if ("cached-player".equals(name)) {
            return Tag.selfClosingInserting(Component.text("Mirai"));
        }
        if ("cached-rank".equals(name)) {
            return Tag.selfClosingInserting(Component.text("管理员"));
        }
        return null;
    }

    private static net.kyori.adventure.text.minimessage.MiniMessage defaultCachingParser() {
        final net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.WithoutArguments dynamic =
                MiniMessagePerformanceBenchmark::dynamicDefaultTag;
        return net.kyori.adventure.text.minimessage.MiniMessage.builder()
                .tags(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.caching(dynamic))
                .postProcessor(UnaryOperator.identity())
                .build();
    }

    private static net.kyori.adventure.text.minimessage.tag.Tag dynamicDefaultTag(final String name) {
        if ("cached-player".equals(name)) {
            return net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(Component.text("Mirai"));
        }
        if ("cached-rank".equals(name)) {
            return net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(Component.text("管理员"));
        }
        return null;
    }

    private static top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage sparrowLargeStaticParser() {
        final top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver.Builder builder =
                top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver.builder();
        for (int index = 0; index < MiniMessageBenchmarkFixtures.LARGE_STATIC_RESOLVER_COUNT; index++) {
            final String name = staticTagName(index);
            builder.resolver(top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver.resolver(
                    name, Tag.selfClosingInserting(Component.text(name))));
        }
        return top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage.builder()
                .tags(builder.build())
                .postProcessor(UnaryOperator.identity())
                .build();
    }

    private static net.kyori.adventure.text.minimessage.MiniMessage defaultLargeStaticParser() {
        final net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder builder =
                net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.builder();
        for (int index = 0; index < MiniMessageBenchmarkFixtures.LARGE_STATIC_RESOLVER_COUNT; index++) {
            final String name = staticTagName(index);
            builder.resolver(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver(
                    name, net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(Component.text(name))));
        }
        return net.kyori.adventure.text.minimessage.MiniMessage.builder()
                .tags(builder.build())
                .postProcessor(UnaryOperator.identity())
                .build();
    }

    private static String staticTagName(final int index) {
        return String.format("tag%02d", index);
    }
}
