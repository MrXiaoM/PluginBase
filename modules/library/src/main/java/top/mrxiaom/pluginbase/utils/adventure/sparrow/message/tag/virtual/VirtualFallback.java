package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.virtual;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.TokenEmitter;
import top.mrxiaom.pluginbase.utils.adventure.text.VirtualFallbackComponent;
import top.mrxiaom.pluginbase.utils.adventure.text.VirtualFallbackComponentRenderer;

import java.util.Collections;
import java.util.function.Consumer;

public class VirtualFallback implements VirtualOperation {
    @Override
    public @Nullable Emitable claimComponent(Component comp) {
        if (!(comp instanceof VirtualFallbackComponent)) {
            return null;
        }
        VirtualFallbackComponent<?> virtualComponent = (VirtualFallbackComponent<?>) comp;

        final VirtualFallbackComponentRenderer<?> holder = virtualComponent.renderer();
        if (!(holder instanceof TagInfoHolder)) {
            return null;
        }

        return (TagInfoHolder) holder;
    }

    @Override
    public @Nullable String content(Component comp) {
        if (!(comp instanceof VirtualFallbackComponent)) {
            return null;
        }
        return ((VirtualFallbackComponent<?>) comp).content();
    }

    @Override
    public Component createVirtualTagInfoHolder(Consumer<TokenEmitter> preserveData, Component current) {
        return VirtualFallbackComponent.createVirtual(Void.class, new TagInfoHolder(preserveData, current), Collections.emptyList(), current.style());
    }

    protected static class TagInfoHolder implements VirtualFallbackComponentRenderer<Void>, Emitable {
        private Consumer<TokenEmitter> output;
        private Component substitute;

        private TagInfoHolder(Consumer<TokenEmitter> output, Component substitute) {
            this.output = output;
            this.substitute = substitute;
        }

        public Consumer<TokenEmitter> output() {
            return output;
        }

        public void output(Consumer<TokenEmitter> output) {
            this.output = output;
        }

        @Override
        public @Nullable Component substitute() {
            return substitute;
        }

        public void substitute(Component substitute) {
            this.substitute = substitute;
        }

        @Override
        public @UnknownNullability ComponentLike apply(final @NotNull Void context) {
            return this.substitute;
        }

        @Override
        public @NotNull String fallbackString() {
            return ""; // only holds data for reserialization, not for display
        }

        @Override
        public void emit(final TokenEmitter emitter) {
            this.output.accept(emitter);
        }
    }
}
