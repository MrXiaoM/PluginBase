/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2025 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver;

import net.kyori.adventure.text.Component;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.ClaimConsumer;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A resolver compiled at build time from an ordered list of child resolvers.
 *
 * <p>All statically known tag names are flattened into a single hash dispatch table,
 * making the common lookup O(1) regardless of how many resolvers were registered.
 * Resolvers with dynamic name sets (hex colors, predicate matchers) are kept on a short
 * ordered fallback chain that is only consulted on a dispatch-table miss (or when the
 * mapped resolver produced no tag / failed).</p>
 *
 * <p>Priority: later-registered resolvers win for equal names within the table, and the
 * fallback chain is consulted in reverse registration order. Note one deliberate deviation
 * from a fully sequential scan: a statically known name always beats a dynamic resolver,
 * even if the dynamic resolver was registered later.</p>
 */
final class CompiledTagResolver implements TagResolver, SerializableResolver {
    private final Map<String, TagResolver> dispatch;
    private final TagResolver[] dynamic; // reverse registration order (last registered first)
    private final boolean anyPreProcess;

    CompiledTagResolver(final Map<String, TagResolver> dispatch, final TagResolver[] dynamic, final boolean anyPreProcess) {
        this.dispatch = dispatch;
        this.dynamic = dynamic;
        this.anyPreProcess = anyPreProcess;
    }

    @Override
    public @Nullable Tag resolve(final String name, final ArgumentQueue arguments, final Context ctx) throws ParsingException {
        ParsingException thrown = null;

        final TagResolver mapped = this.dispatch.get(name);
        if (mapped != null) {
            try {
                final Tag tag = mapped.resolve(name, arguments, ctx);
                if (tag != null) {
                    return tag;
                }
            } catch (final ParsingException ex) {
                arguments.reset();
                thrown = ex;
            } catch (final Exception ex) {
                arguments.reset();
                thrown = ctx.newException("Exception thrown while parsing <" + name + ">", ex, arguments);
            }
            if (this.dynamic.length == 0) {
                if (thrown != null) {
                    throw thrown;
                }
                return null;
            }
        }

        for (final TagResolver resolver : this.dynamic) {
            try {
                if (!resolver.has(name)) continue;
                final Tag tag = resolver.resolve(name, arguments, ctx);
                if (tag != null) {
                    return tag;
                }
            } catch (final ParsingException ex) {
                arguments.reset();
                if (thrown == null) {
                    thrown = ex;
                } else {
                    thrown.addSuppressed(ex);
                }
            } catch (final Exception ex) {
                arguments.reset();
                final ParsingException err = ctx.newException("Exception thrown while parsing <" + name + ">", ex, arguments);
                if (thrown == null) {
                    thrown = err;
                } else {
                    thrown.addSuppressed(err);
                }
            }
        }

        if (thrown != null) {
            throw thrown;
        }
        return null;
    }

    @Override
    public boolean has(final String name) {
        if (this.dispatch.containsKey(name)) {
            return true;
        }
        for (final TagResolver resolver : this.dynamic) {
            if (resolver.has(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void contributeKnownNames(final BiConsumer<String, TagResolver> sink) {
        this.dispatch.forEach(sink);
    }

    @Override
    public boolean isExhaustivelyKnown() {
        return this.dynamic.length == 0;
    }

    @Override
    public boolean mayProducePreProcess() {
        return this.anyPreProcess;
    }

    @Override
    public boolean mayProducePreProcess(final String name) {
        final TagResolver mapped = this.dispatch.get(name);
        if (mapped != null && mapped.mayProducePreProcess()) {
            return true;
        }
        for (final TagResolver resolver : this.dynamic) {
            if (resolver.mayProducePreProcess(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handle(final Component serializable, final ClaimConsumer consumer) {
        final IdentityHashMap<TagResolver, Boolean> seen = new IdentityHashMap<>();
        for (final TagResolver resolver : this.dispatch.values()) {
            if (seen.put(resolver, Boolean.TRUE) == null && resolver instanceof SerializableResolver) {
                SerializableResolver serializableResolver = (SerializableResolver) resolver;
                serializableResolver.handle(serializable, consumer);
            }
        }
        for (final TagResolver resolver : this.dynamic) {
            if (seen.put(resolver, Boolean.TRUE) == null && resolver instanceof SerializableResolver) {
                SerializableResolver serializableResolver = (SerializableResolver) resolver;
                serializableResolver.handle(serializable, consumer);
            }
        }
    }
}
