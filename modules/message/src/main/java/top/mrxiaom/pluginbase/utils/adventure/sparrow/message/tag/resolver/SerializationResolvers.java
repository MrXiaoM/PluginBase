package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver;

import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Collects the unique {@link SerializableResolver} instances that should participate
 * in MiniMessage serialization.
 *
 * <p>Name-to-resolver dispatch tables store the same resolver under many keys. Building
 * the unique list once at construction time keeps the serialization hot path from
 * allocating an identity map and scanning every tag name for each component.</p>
 */
final class SerializationResolvers {
    static final SerializableResolver[] EMPTY = new SerializableResolver[0];

    private SerializationResolvers() {
    }

    static SerializableResolver[] unique(final TagResolver[] resolvers) {
        return unique(null, resolvers);
    }

    static SerializableResolver[] unique(final Iterable<? extends TagResolver> first, final TagResolver[] second) {
        final IdentityHashMap<TagResolver, Boolean> seen = new IdentityHashMap<>();
        final List<SerializableResolver> collected = new ArrayList<>();
        if (first != null) {
            for (final TagResolver resolver : first) {
                addUnique(seen, collected, resolver);
            }
        }
        if (second != null) {
            for (final TagResolver resolver : second) {
                addUnique(seen, collected, resolver);
            }
        }
        if (collected.isEmpty()) {
            return EMPTY;
        }
        return collected.toArray(EMPTY);
    }

    private static void addUnique(
            final IdentityHashMap<TagResolver, Boolean> seen,
            final List<SerializableResolver> collected,
            final TagResolver resolver
    ) {
        if (resolver == null || resolver == EmptyTagResolver.INSTANCE) {
            return;
        }
        if (!(resolver instanceof SerializableResolver)) {
            return;
        }
        if (seen.put(resolver, Boolean.TRUE) != null) {
            return;
        }
        collected.add((SerializableResolver) resolver);
    }
}
