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

import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * A lightweight pair of resolvers consulted in priority order, used to combine a
 * MiniMessage instance's tags with per-call extra tags without recompiling anything.
 */
public final class PrioritizedTagResolver implements TagResolver, Removable {
    private TagResolver primary;
    private TagResolver secondary;

    public PrioritizedTagResolver(final TagResolver primary, final TagResolver secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public void remove(String tagName) {
        if (primary.has(tagName)) {
            if (primary instanceof Removable) {
                ((Removable) primary).remove(tagName);
            } else {
                primary = EmptyTagResolver.INSTANCE;
            }
        }
        if (secondary.has(tagName)) {
            if (secondary instanceof Removable) {
                ((Removable) secondary).remove(tagName);
            } else {
                secondary = EmptyTagResolver.INSTANCE;
            }
        }
    }

    @Override
    public @Nullable Tag resolve(final String name, final ArgumentQueue arguments, final Context ctx) throws ParsingException {
        ParsingException thrown = null;
        try {
            final Tag tag = this.primary.has(name) ? this.primary.resolve(name, arguments, ctx) : null;
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
        try {
            final Tag tag = this.secondary.has(name) ? this.secondary.resolve(name, arguments, ctx) : null;
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
        if (thrown != null) {
            throw thrown;
        }
        return null;
    }

    @Override
    public boolean has(final String name) {
        return this.primary.has(name) || this.secondary.has(name);
    }

    @Override
    public boolean mayProducePreProcess() {
        return this.primary.mayProducePreProcess() || this.secondary.mayProducePreProcess();
    }

    @Override
    public boolean mayProducePreProcess(final String name) {
        return this.primary.mayProducePreProcess(name) || this.secondary.mayProducePreProcess(name);
    }
}
