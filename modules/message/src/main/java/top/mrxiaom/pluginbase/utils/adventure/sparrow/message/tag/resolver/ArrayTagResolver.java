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

import java.util.function.BiConsumer;

/**
 * A lightweight ordered resolver used for small resolver sets — typically per-call
 * extra tags built on the fly. It performs no compilation (no hash map allocation),
 * scanning its children in reverse registration order instead, which exactly matches
 * "last registered wins" semantics. For a handful of resolvers this is faster than
 * compiling a dispatch table that would be used for a single parse.
 */
final class ArrayTagResolver implements TagResolver, Removable, SerializableResolver {
    private final TagResolver[] resolvers; // reverse registration order (last registered first)
    private SerializableResolver[] serializers;
    private final boolean anyPreProcess;
    private final boolean exhaustivelyKnown;

    ArrayTagResolver(final TagResolver[] resolvers) {
        this.resolvers = resolvers;
        boolean preProcess = false;
        boolean exhaustive = true;
        for (final TagResolver resolver : resolvers) {
            preProcess |= resolver.mayProducePreProcess();
            exhaustive &= resolver.isExhaustivelyKnown();
        }
        this.anyPreProcess = preProcess;
        this.exhaustivelyKnown = exhaustive;
        this.serializers = SerializationResolvers.unique(resolvers);
    }

    @Override
    public void remove(String tagName) {
        boolean changed = false;
        for (int i = 0; i < resolvers.length; i++) {
            TagResolver resolver = resolvers[i];
            if (resolver.has(tagName)) {
                if (resolver instanceof Removable) {
                    ((Removable) resolver).remove(tagName);
                } else {
                    resolvers[i] = EmptyTagResolver.INSTANCE;
                }
                changed = true;
            }
        }
        if (changed) {
            this.serializers = SerializationResolvers.unique(resolvers);
        }
    }

    @Override
    public @Nullable Tag resolve(final String name, final ArgumentQueue arguments, final Context ctx) throws ParsingException {
        ParsingException thrown = null;
        for (final TagResolver resolver : this.resolvers) {
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
        for (final TagResolver resolver : this.resolvers) {
            if (resolver.has(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void contributeKnownNames(final BiConsumer<String, TagResolver> sink) {
        // later registrations (earlier in this array) must win when a parent compiles us
        for (int i = this.resolvers.length - 1; i >= 0; i--) {
            this.resolvers[i].contributeKnownNames(sink);
        }
    }

    @Override
    public boolean isExhaustivelyKnown() {
        return this.exhaustivelyKnown;
    }

    @Override
    public boolean mayProducePreProcess() {
        return this.anyPreProcess;
    }

    @Override
    public boolean mayProducePreProcess(final String name) {
        for (final TagResolver resolver : this.resolvers) {
            if (resolver.mayProducePreProcess(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handle(final Component serializable, final ClaimConsumer consumer) {
        for (final SerializableResolver resolver : this.serializers) {
            resolver.handle(serializable, consumer);
        }
    }
}
