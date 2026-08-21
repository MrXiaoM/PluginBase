package top.mrxiaom.pluginbase.api.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.adventure.TagPattern;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface ITagSerializer {
    @NotNull String serialize(@NotNull Component input);

    @Contract(value = "!null -> !null; null -> null", pure = true)
    default @Nullable String serializeOrNull(final @Nullable Component component) {
        return this.serializeOr(component, null);
    }

    @Contract(value = "!null, _ -> !null; null, _ -> param2", pure = true)
    default @Nullable String serializeOr(final @Nullable Component component, final @Nullable String fallback) {
        if (component == null) return fallback;

        return this.serialize(component);
    }

    @NotNull Component deserialize(@NotNull String input);

    @Contract(value = "!null -> !null; null -> null", pure = true)
    default @Nullable Component deserializeOrNull(final @Nullable String input) {
        return this.deserializeOr(input, null);
    }

    @Contract(value = "!null, _ -> !null; null, _ -> param2", pure = true)
    default @Nullable Component deserializeOr(final @Nullable String input, final @Nullable Component fallback) {
        if (input == null) return fallback;

        return this.deserialize(input);
    }

    interface Builder {
        // TODO: 设计并实现添加标签相关接口

        @NotNull Builder editTags(@NotNull Consumer<TagBuilder> consumer);

        @NotNull Builder postProcessor(final @NotNull UnaryOperator<Component> postProcessor);
        @NotNull Builder preProcessor(final @NotNull UnaryOperator<String> preProcessor);

        @NotNull ITagSerializer build();
    }

    interface TagBuilder {
        @NotNull TagBuilder removeTags(@NotNull Iterable<String> tagNames);

        @ApiStatus.Experimental
        @NotNull TagBuilder addInserting(@TagPattern String name, @NotNull Component component);
        @ApiStatus.Experimental
        default @NotNull TagBuilder addInserting(@TagPattern String name, @NotNull ComponentLike value) {
            return addInserting(name, value.asComponent());
        }
        @ApiStatus.Experimental
        @NotNull TagBuilder addSelfClosingInserting(@TagPattern String name, @NotNull Component component);
        @ApiStatus.Experimental
        default @NotNull TagBuilder addSelfClosingInserting(@TagPattern String name, @NotNull ComponentLike value) {
            return addSelfClosingInserting(name, value.asComponent());
        }
        @ApiStatus.Experimental
        @NotNull TagBuilder addStyling(@TagPattern String name, @NotNull Consumer<Style.Builder> styles);
        @ApiStatus.Experimental
        @NotNull TagBuilder addStyling(@TagPattern String name, @NotNull StyleBuilderApplicable @NotNull... actions);

        @ApiStatus.Experimental
        @NotNull TagBuilder addHoverTag(String name, HoverEventSource<?> source);
    }
}
