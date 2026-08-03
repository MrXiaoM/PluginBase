package top.mrxiaom.pluginbase.utils.adventure.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("NonExtendableApiUsage")
public class AdventureComponentSerializerImpl<I extends Component, O extends Component, R> implements AdventureComponentSerializer<I, O, R> {
    private final ComponentSerializer<I, O, R> impl;
    public AdventureComponentSerializerImpl(ComponentSerializer<I, O, R> impl) {
        this.impl = impl;
    }

    @Override
    public @NotNull O deserialize(@NotNull R input) {
        return impl.deserialize(input);
    }

    @Override
    public @NotNull R serialize(@NotNull I component) {
        return impl.serialize(component);
    }
}
