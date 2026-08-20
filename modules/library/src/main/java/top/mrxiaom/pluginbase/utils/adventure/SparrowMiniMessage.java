package top.mrxiaom.pluginbase.utils.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.api.message.ITagSerializer;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.MiniMessage;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.StyleClaim;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class SparrowMiniMessage implements ITagSerializer {
    private final MiniMessage miniMessage;

    private SparrowMiniMessage(MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
    }

    @Override
    public @NotNull String serialize(@NotNull Component input) {
        return miniMessage.serialize(input);
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input) {
        return miniMessage.deserialize(input);
    }

    public static ITagSerializer from(MiniMessage miniMessage) {
        return new SparrowMiniMessage(miniMessage);
    }

    public static ITagSerializer.Builder builder() {
        return builder(MiniMessage.builder());
    }

    public static ITagSerializer.Builder builder(MiniMessage.Builder builder) {
        return new Builder(builder);
    }

    public static class Builder implements ITagSerializer.Builder {
        private final MiniMessage.Builder builder;

        private Builder(MiniMessage.Builder builder) {
            this.builder = builder;
        }

        @Override
        public ITagSerializer.@NotNull Builder editTags(@NotNull Consumer<ITagSerializer.TagBuilder> consumer) {
            builder.editTags(it -> {
                TagBuilder wrapper = new TagBuilder(it);
                consumer.accept(wrapper);
            });
            return this;
        }

        @Override
        public @NotNull ITagSerializer.Builder postProcessor(@NotNull UnaryOperator<Component> postProcessor) {
            builder.postProcessor(postProcessor);
            return this;
        }

        @Override
        public @NotNull ITagSerializer.Builder preProcessor(@NotNull UnaryOperator<String> preProcessor) {
            builder.preProcessor(preProcessor);
            return this;
        }

        @Override
        public @NotNull ITagSerializer build() {
            return from(builder.build());
        }
    }

    public static class TagBuilder implements ITagSerializer.TagBuilder {
        private final TagResolver.Builder tags;
        public TagBuilder(TagResolver.Builder tags) {
            this.tags = tags;
        }

        @Override
        public ITagSerializer.@NotNull TagBuilder removeTags(@NotNull Iterable<String> tagNames) {
            tags.resolvers().removeIf(it -> {
                for (String tag : tagNames) {
                    if (it.has(tag)) return true;
                }
                return false;
            });
            return this;
        }

        @Override
        public ITagSerializer.@NotNull TagBuilder addInserting(@TagPattern String name, @NotNull Component component) {
            tags.tag(name, Tag.inserting(component));
            return this;
        }

        @Override
        public ITagSerializer.@NotNull TagBuilder addSelfClosingInserting(@TagPattern String name, @NotNull Component component) {
            tags.tag(name, Tag.selfClosingInserting(component));
            return this;
        }

        @Override
        public ITagSerializer.@NotNull TagBuilder addStyling(@TagPattern String name, @NotNull Consumer<Style.Builder> styles) {
            tags.tag(name, Tag.styling(styles));
            return this;
        }

        @Override
        public ITagSerializer.@NotNull TagBuilder addStyling(@TagPattern String name, @NotNull StyleBuilderApplicable @NotNull ... actions) {
            tags.tag(name, Tag.styling(actions));
            return this;
        }

        @Override
        public ITagSerializer.@NotNull TagBuilder addHoverTag(String name, HoverEventSource<?> source) {
            tags.resolver(SerializableResolver.claimingStyle(
                    name,
                    (args, ctx) -> Tag.styling(source.asHoverEvent()),
                    StyleClaim.claim(
                            name,
                            Style::hoverEvent,
                            (event, emitter) -> emitter.tag(name)
                    )));
            return this;
        }
    }
}