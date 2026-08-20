package top.mrxiaom.pluginbase.utils.adventure.text;

import net.kyori.adventure.internal.Internals;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VirtualFallbackComponent<C> implements TextComponent {
    protected final List<Component> children;
    protected final Style style;
    private final Class<C> contextType;
    private final VirtualFallbackComponentRenderer<C> renderer;

    public static <C> VirtualFallbackComponent<C> createVirtual(final @NotNull Class<C> contextType, final @NotNull VirtualFallbackComponentRenderer<C> renderer) {
        return createVirtual(contextType, renderer, Collections.emptyList(), Style.empty());
    }

    public static <C> VirtualFallbackComponent<C> createVirtual(final @NotNull Class<C> contextType, final @NotNull VirtualFallbackComponentRenderer<C> renderer, final List<? extends ComponentLike> children, final Style style) {
        final List<Component> filteredChildren = ComponentLike.asComponents(children, IS_NOT_EMPTY);

        return new VirtualFallbackComponent<>(filteredChildren, style, contextType, renderer);
    }

    VirtualFallbackComponent(final @NotNull List<Component> children, final @NotNull Style style, final @NotNull Class<C> contextType, final @NotNull VirtualFallbackComponentRenderer<C> renderer) {
        this.children = children;
        this.style = style;
        this.contextType = contextType;
        this.renderer = renderer;
    }

    @Override
    public @NotNull List<Component> children() {
        return children;
    }

    @Override
    public @NotNull Style style() {
        return style;
    }

    public Class<C> contextType() {
        return contextType;
    }

    public VirtualFallbackComponentRenderer<C> renderer() {
        return renderer;
    }

    @Override
    public @NotNull String content() {
        return this.renderer.fallbackString();
    }

    @Override
    public @NotNull TextComponent content(final @NotNull String content) {
        return this;
    }

    @Override
    public @NotNull TextComponent children(final @NotNull List<? extends ComponentLike> children) {
        return createVirtual(contextType, renderer, children, this.style);
    }

    @Override
    public @NotNull TextComponent style(final @NotNull Style style) {
        return createVirtual(contextType, renderer, this.children, style);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (this == other) return true;
        if (!(other instanceof VirtualFallbackComponent)) return false;
        if (!super.equals(other)) return false;
        final VirtualFallbackComponent<?> that = (VirtualFallbackComponent<?>) other;
        return Objects.equals(this.children, that.children)
                && Objects.equals(this.style, that.style)
                && Objects.equals(this.contextType, that.contextType)
                && Objects.equals(this.renderer, that.renderer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, style, contextType, renderer);
    }

    @Override
    public String toString() {
        return Internals.toString(this);
    }

    @Override
    public @NotNull Builder toBuilder() {
        return Component.text()
                .append(children)
                .style(style);
    }
}
