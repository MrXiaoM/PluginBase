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

import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.TagInternals;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

final class TagResolverBuilderImpl implements TagResolver.Builder {
    static final Collector<TagResolver, TagResolver.Builder, TagResolver> COLLECTOR = Collector.of(
            TagResolver::builder,
            TagResolver.Builder::resolver,
            (left, right) -> TagResolver.builder().resolvers(left.build(), right.build()),
            TagResolver.Builder::build
    );

    private final Map<String, Tag> replacements = new HashMap<>();
    private final List<TagResolver> resolvers = new ArrayList<>();

    @Override
    public TagResolver.Builder tag(final String name, final Tag tag) {
        TagInternals.assertValidTagName(requireNonNull(name, "name"));
        this.replacements.put(
                name,
                requireNonNull(tag, "tag")
        );
        return this;
    }

    @Override
    public TagResolver.Builder resolver(final TagResolver resolver) {
        if (!this.consumePotentialMappable(resolver)) {
            this.popMap();
            this.resolvers.add(requireNonNull(resolver, "resolver"));
        }
        return this;
    }

    @Override
    public TagResolver.Builder resolvers(final TagResolver... resolvers) {
        requireNonNull(resolvers, "resolvers");
        boolean popped = false;
        for (final TagResolver resolver : resolvers) {
            popped = this.single(resolver, popped);
        }
        return this;
    }

    @Override
    public TagResolver.Builder resolvers(final Iterable<? extends TagResolver> resolvers) {
        boolean popped = false;
        for (final TagResolver resolver : requireNonNull(resolvers, "resolvers")) {
            popped = this.single(resolver, popped);
        }
        return this;
    }

    @Override
    public List<TagResolver> resolvers() {
        return resolvers;
    }

    private boolean single(final TagResolver resolver, final boolean popped) {
        if (!this.consumePotentialMappable(resolver)) {
            if (!popped) {
                this.popMap();
            }
            this.resolvers.add(requireNonNull(resolver, "resolvers[?]"));
            return true;
        }
        return false;
    }

    private void popMap() {
        if (!this.replacements.isEmpty()) {
            this.resolvers.add(new MapTagResolver(new HashMap<>(this.replacements)));
            this.replacements.clear();
        }
    }

    private boolean consumePotentialMappable(final TagResolver resolver) {
        if (resolver instanceof MappableResolver) {
            MappableResolver mappable = (MappableResolver) resolver;
            return mappable.contributeToMap(this.replacements);
        } else {
            return false;
        }
    }

    /**
     * Resolver sets at or below this size are not compiled into a dispatch table: scanning a
     * handful of resolvers linearly is cheaper than building a hash map, especially for
     * per-call resolver sets that are built for a single parse and then discarded.
     */
    private static final int COMPILE_THRESHOLD = 8;

    @Override
    public TagResolver build() {
        this.popMap();
        if (this.resolvers.isEmpty()) {
            return EmptyTagResolver.INSTANCE;
        }

        if (this.resolvers.size() == 1) {
            return this.resolvers.get(0);
        }

        if (this.resolvers.size() <= COMPILE_THRESHOLD) {
            // small set: consult in reverse registration order (last registered wins), no compilation
            final TagResolver[] array = new TagResolver[this.resolvers.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = this.resolvers.get(array.length - 1 - i);
            }
            return new ArrayTagResolver(array);
        }

        // Compile the ordered resolver list into a dispatch table + dynamic fallback chain.
        // Later registrations overwrite earlier ones for equal names, matching the
        // "last specified resolver takes priority" contract.
        final Map<String, TagResolver> dispatch = new HashMap<>();
        final List<TagResolver> dynamic = new ArrayList<>();
        boolean anyPreProcess = false;
        for (final TagResolver resolver : this.resolvers) {
            resolver.contributeKnownNames(dispatch::put);
            if (!resolver.isExhaustivelyKnown()) {
                dynamic.add(resolver);
            }
            anyPreProcess |= resolver.mayProducePreProcess();
        }
        // the fallback chain is consulted in reverse registration order
        final TagResolver[] dynamicArray = new TagResolver[dynamic.size()];
        for (int i = 0; i < dynamicArray.length; i++) {
            dynamicArray[i] = dynamic.get(dynamic.size() - 1 - i);
        }
        return new CompiledTagResolver(dispatch, dynamicArray, anyPreProcess);
    }
}
