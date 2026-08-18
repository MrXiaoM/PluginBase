package top.mrxiaom.pluginbase.utils.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.api.ITagSerializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.UnaryOperator;

public class DefaultMiniMessage implements ITagSerializer {
    private final MiniMessage miniMessage;
    private DefaultMiniMessage(MiniMessage miniMessage) {
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
        return new DefaultMiniMessage(miniMessage);
    }

    public static ITagSerializer.Builder builder() {
        return builder(MiniMessage.builder());
    }

    public static ITagSerializer.Builder builder(MiniMessage.Builder builder) {
        return new Builder(builder);
    }

    public static class Builder implements ITagSerializer.Builder {
        private static Field resolversField;
        private final MiniMessage.Builder builder;
        private Builder(MiniMessage.Builder builder) {
            this.builder = builder;
        }

        @SuppressWarnings({"unchecked", "SameParameterValue"})
        public static void remove(TagResolver.Builder builder, Iterable<String> tags) {
            try {
                if (resolversField == null) {
                    resolversField = builder.getClass().getDeclaredField("resolvers");
                    resolversField.setAccessible(true);
                }
                List<TagResolver> list = (List<TagResolver>) resolversField.get(builder);
                list.removeIf(it -> {
                    for (String tag : tags) {
                        if (it.has(tag)) return true;
                    }
                    return false;
                });
            } catch (Throwable ignored) {
            }
        }

        @Override
        public @NotNull ITagSerializer.Builder removeTags(@NotNull Iterable<String> tagNames) {
            builder.editTags(tags -> remove(tags, tagNames));
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
}
