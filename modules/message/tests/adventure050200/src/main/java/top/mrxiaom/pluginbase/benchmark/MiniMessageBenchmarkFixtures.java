package top.mrxiaom.pluginbase.benchmark;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;
import top.mrxiaom.pluginbase.utils.adventure.DefaultMiniMessage;
import top.mrxiaom.pluginbase.utils.adventure.SparrowMiniMessage;

import java.util.Collections;
import java.util.function.UnaryOperator;

/**
 * 为 SparrowMiniMessage 与 DefaultMiniMessage 提供语义相同的基准样本和配置。
 *
 * <p>共同样本只能使用两个实现都支持、且由 {@link #assertEquivalent()} 验证过的语法。
 * 解析器机制样本则用于单独测量无参数动态标签缓存和大型静态解析器集的查找成本。</p>
 */
public final class MiniMessageBenchmarkFixtures {
    public static final String BASIC = "欢迎, player! \\<不是标签> <not-a-known-tag>";
    public static final String STYLED = "<red>红色 <bold>粗体</bold></red> <#55ff55><underlined>十六进制下划线</underlined></#55ff55>";
    public static final String INTERACTIVE = "<click:run_command:'/say hello'><hover:show_text:'<yellow>详细说明</yellow>'><insertion:copied>点击这里</insertion></hover></click>";
    public static final String CUSTOM = "<player/>，<warning>服务器将在 <notice/> 后重启</warning>";
    /**
     * 只使用两端在 Adventure 5.2.0 下可验证为等价的标准渐变语法。
     * rainbow 与 transition 的组件树表示因实现策略不同，不用于直接性能排名。
     */
    public static final String GRADIENT = "<gradient:#ff0000:#00ff00>渐变文字</gradient>";

    public static final String DYNAMIC_HIT = "<cached-player/> <cached-player/> <cached-rank/>";
    public static final String DYNAMIC_MISS = "<unknown-placeholder/> <unknown-placeholder/>";
    public static final String LARGE_STATIC = "<tag00/><tag08/><tag15/>";
    public static final int LARGE_STATIC_RESOLVER_COUNT = 16;

    private MiniMessageBenchmarkFixtures() {
    }

    public static ITagSerializer sparrow() {
        return configure(SparrowMiniMessage.builder());
    }

    public static ITagSerializer defaultMiniMessage() {
        return configure(DefaultMiniMessage.builder());
    }

    private static ITagSerializer configure(final ITagSerializer.Builder builder) {
        return builder
                .editTags(tags -> tags
                        .removeTags(Collections.singleton("pride"))
                        .addSelfClosingInserting("player", Component.text("Mirai", NamedTextColor.AQUA))
                        .addStyling("warning", style -> style.color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true))
                        .addHoverTag("notice", Component.text("预计五分钟")))
                // 两端均禁用默认 compact，避免把不同的后处理行为混入解析比较。
                .postProcessor(UnaryOperator.identity())
                .build();
    }

    public static Component serializationBasicComponent() {
        return Component.text("欢迎, player! <需要转义>");
    }

    public static Component serializationStyledComponent() {
        return Component.text("红色 ")
                .color(NamedTextColor.RED)
                .append(Component.text("粗体").decorate(TextDecoration.BOLD));
    }

    public static Component serializationInteractiveComponent() {
        return Component.text("点击这里")
                .clickEvent(ClickEvent.runCommand("/say hello"))
                .hoverEvent(Component.text("详细说明", NamedTextColor.YELLOW))
                .insertion("copied");
    }

    public static Component serializationGradientComponent(final ITagSerializer serializer) {
        return serializer.deserialize(GRADIENT);
    }

    public static String visibleText(final Component component) {
        final StringBuilder result = new StringBuilder();
        appendVisibleText(component, result);
        return result.toString();
    }

    private static void appendVisibleText(final Component component, final StringBuilder result) {
        if (component instanceof net.kyori.adventure.text.TextComponent) {
            result.append(((net.kyori.adventure.text.TextComponent) component).content());
        }
        for (Component child : component.children()) {
            appendVisibleText(child, result);
        }
    }

    public static String sample(final String scenario) {
        switch (scenario) {
            case "basic":
                return BASIC;
            case "styled":
                return STYLED;
            case "interactive":
                return INTERACTIVE;
            case "custom":
                return CUSTOM;
            case "gradient":
                return GRADIENT;
            default:
                throw new IllegalArgumentException("未知共同基准场景: " + scenario);
        }
    }

    /**
     * 验证可直接比较的共同解析场景在两种实现中产出规范化后完全相同的 Component。
     * 基准测试在其 {@code @Setup} 中调用此方法，JUnit 测试也会单独调用它。
     */
    public static void assertEquivalent() {
        final ITagSerializer sparrow = sparrow();
        final ITagSerializer defaultMiniMessage = defaultMiniMessage();
        for (String scenario : new String[]{"basic", "styled", "interactive", "custom"}) {
            final String input = sample(scenario);
            final Component sparrowResult = sparrow.deserialize(input).compact();
            final Component defaultResult = defaultMiniMessage.deserialize(input).compact();
            if (!sparrowResult.equals(defaultResult)) {
                throw new AssertionError("场景 " + scenario + " 的规范化反序列化结果不等价\nSparrow: "
                        + sparrowResult + "\nDefault: " + defaultResult);
            }
        }
    }
}
