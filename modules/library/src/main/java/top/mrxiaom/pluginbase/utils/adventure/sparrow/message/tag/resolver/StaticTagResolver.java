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

import java.util.function.BiConsumer;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.TagInternals;

/**
 * Base class for resolvers with a fixed set of statically known tag names.
 *
 * <p>Extending this class (instead of implementing {@link TagResolver} directly) makes the
 * resolver's names enumerable at build time, so it is compiled into the dispatch table and
 * looked up in O(1). A plain {@code TagResolver} implementation without these overrides is
 * treated as dynamic and stays on the linear fallback chain — which is correct for
 * unbounded name sets (hex colors, pattern matching) but silently wasteful for fixed names.</p>
 *
 * <p>Per the composite-resolver contract, {@link #resolve} is only invoked with names for
 * which {@link #has(String)} returned {@code true}, so implementations only need to handle
 * their declared names.</p>
 */
public abstract class StaticTagResolver implements TagResolver {
    private final String[] names;

    protected StaticTagResolver(final String... names) {
        for (final String name : names) {
            TagInternals.assertValidTagName(name);
        }
        this.names = names.clone();
    }

    @Override
    public boolean has(final String name) {
        for (final String own : this.names) {
            if (own.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void contributeKnownNames(final BiConsumer<String, TagResolver> sink) {
        for (final String name : this.names) {
            sink.accept(name, this);
        }
    }

    @Override
    public boolean isExhaustivelyKnown() {
        return true;
    }
}
