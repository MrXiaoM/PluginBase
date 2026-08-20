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

import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StyleClaimImpl<V> implements StyleClaim<V> {
    private String claimKey;
    private Function<Style, V> lens;
    private Predicate<V> filter;
    private BiConsumer<V, TokenEmitter> emitable;

    public StyleClaimImpl(String claimKey, Function<Style, V> lens, Predicate<V> filter, BiConsumer<V, TokenEmitter> emitable) {
        this.claimKey = claimKey;
        this.lens = lens;
        this.filter = filter;
        this.emitable = emitable;
    }

    @Override
    public String claimKey() {
        return claimKey;
    }

    public void claimKey(String claimKey) {
        this.claimKey = claimKey;
    }

    public Function<Style, V> lens() {
        return lens;
    }

    public void lens(Function<Style, V> lens) {
        this.lens = lens;
    }

    public Predicate<V> filter() {
        return filter;
    }

    public void filter(Predicate<V> filter) {
        this.filter = filter;
    }

    public BiConsumer<V, TokenEmitter> emitable() {
        return emitable;
    }

    public void emitable(BiConsumer<V, TokenEmitter> emitable) {
        this.emitable = emitable;
    }

    @Override
    public @Nullable Emitable apply(final Style style) {
        final V element = this.lens.apply(style);
        if (element == null || !this.filter.test(element)) {
            return null;
        }

        return emitter -> this.emitable.accept(element, emitter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.claimKey);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StyleClaimImpl<?>)) {
            return false;
        }
        StyleClaimImpl<?> that = (StyleClaimImpl<?>) other;
        return Objects.equals(this.claimKey, that.claimKey);
    }
}
