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
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.standard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.SelectorComponent;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.ArgumentQueue;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

/**
 * Insert a selector component into the result.
 *
 * @since 4.11.0
 */
public final class SelectorTag {
    public static final String SEL = "sel";
    public static final String SELECTOR = "selector";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(
            TagResolver.setOf(SEL, SELECTOR),
            SelectorTag::create,
            SelectorTag::claim
    );

    private SelectorTag() {
    }

    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        final String key = args.popOr("A selection key is required").value();
        ComponentLike separator = null;
        if (args.hasNext()) {
            separator = ctx.deserialize(args.pop().value());
        }

        return Tag.inserting(Component.selector(key, separator));
    }

    static @Nullable Emitable claim(final Component input) {
        if (!(input instanceof SelectorComponent)) return null;
        SelectorComponent st = (SelectorComponent) input;

        return emit -> {
            emit.tag(SEL);
            emit.argument(st.pattern());

            final Component separator = st.separator();
            if (separator != null) {
                emit.argument(separator);
            }
        };
    }
}
