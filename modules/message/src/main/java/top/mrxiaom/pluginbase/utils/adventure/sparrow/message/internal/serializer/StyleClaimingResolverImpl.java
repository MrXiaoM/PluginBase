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
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.ArgumentQueue;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;

import java.util.Set;
import java.util.function.BiFunction;

public class StyleClaimingResolverImpl implements TagResolver, SerializableResolver.Single {
    private Set<String> names;
    private BiFunction<ArgumentQueue, Context, Tag> handler;
    private @Nullable StyleClaim<?> styleClaim;

    public StyleClaimingResolverImpl(Set<String> names, BiFunction<ArgumentQueue, Context, Tag> handler, @Nullable StyleClaim<?> styleClaim) {
        this.names = names;
        this.handler = handler;
        this.styleClaim = styleClaim;
    }

    public Set<String> names() {
        return names;
    }

    public void names(Set<String> names) {
        this.names = names;
    }

    public BiFunction<ArgumentQueue, Context, Tag> handler() {
        return handler;
    }

    public void handler(BiFunction<ArgumentQueue, Context, Tag> handler) {
        this.handler = handler;
    }

    public @Nullable StyleClaim<?> styleClaim() {
        return styleClaim;
    }

    public void styleClaim(@Nullable StyleClaim<?> styleClaim) {
        this.styleClaim = styleClaim;
    }

    @Override
    public @Nullable Tag resolve(final String name, final ArgumentQueue arguments, final Context ctx) throws ParsingException {
        // no name check here: composite resolvers only call resolve() after has(name) passed
        return this.handler.apply(arguments, ctx);
    }

    @Override
    public boolean has(final String name) {
        return this.names.contains(name);
    }

    @Override
    public void contributeKnownNames(final java.util.function.BiConsumer<String, TagResolver> sink) {
        for (final String name : this.names) {
            sink.accept(name, this);
        }
    }

    @Override
    public boolean isExhaustivelyKnown() {
        return true;
    }

    @Override
    public @Nullable StyleClaim<?> claimStyle() {
        return this.styleClaim;
    }
}
