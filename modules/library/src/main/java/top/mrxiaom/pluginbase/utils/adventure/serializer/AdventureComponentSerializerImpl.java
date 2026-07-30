package top.mrxiaom.pluginbase.utils.adventure.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("NonExtendableApiUsage")
public class AdventureComponentSerializerImpl<I extends Component, O extends Component, R> implements AdventureComponentSerializer<I, O, R> {
    private final ComponentSerializer<I, O, R> impl;
    public AdventureComponentSerializerImpl(ComponentSerializer<I, O, R> impl) {
        this.impl = impl;
    }

    @Override
    public @NonNull O deserialize(@NonNull R input) {
        return impl.deserialize(input);
    }

    @Override
    public @NonNull R serialize(@NonNull I component) {
        return impl.serialize(component);
    }
}
