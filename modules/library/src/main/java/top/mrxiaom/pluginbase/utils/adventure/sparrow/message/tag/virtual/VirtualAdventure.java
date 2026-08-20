package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.virtual;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.VirtualComponentRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.TokenEmitter;

import java.util.function.Consumer;

public class VirtualAdventure implements VirtualOperation {
    @Override
    public @Nullable Emitable claimComponent(Component comp) {
        if (!(comp instanceof VirtualComponent)) {
            return null;
        }
        VirtualComponent virtualComponent = (VirtualComponent) comp;

        final VirtualComponentRenderer<?> holder = virtualComponent.renderer();
        if (!(holder instanceof TagInfoHolder)) {
            return null;
        }

        return (TagInfoHolder) holder;
    }

    @Override
    public Component createVirtualTagInfoHolder(Consumer<TokenEmitter> preserveData, Component current) {
        return Component.virtual(Void.class, new TagInfoHolder(preserveData, current), current.style());
    }

    protected static class TagInfoHolder implements VirtualComponentRenderer<Void>, Emitable {
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
