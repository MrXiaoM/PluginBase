package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.virtual;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.TokenEmitter;

import java.util.function.Consumer;

public interface VirtualOperation {
    @Nullable Emitable claimComponent(final Component comp);
    Component createVirtualTagInfoHolder(Consumer<TokenEmitter> preserveData, Component current);

    static VirtualOperation create() {
        try {
            return new VirtualAdventure();
        } catch (Throwable t) {
            return new VirtualFallback();
        }
    }
}
