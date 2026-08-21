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
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.ArgumentQueue;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

/**
 * Newline tag.
 *
 * @since 4.10.0
 */
public final class NewlineTag {
    public static final String BR = "br";
    public static final String NEWLINE = "newline";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(
            TagResolver.setOf(NEWLINE, BR),
            NewlineTag::create,
            NewlineTag::claimComponent
    );

    private NewlineTag() {
    }

    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        return Tag.selfClosingInserting(Component.newline());
    }

    static @Nullable Emitable claimComponent(final Component input) {
        if (Component.newline().equals(input)) {
            return emit -> emit.selfClosingTag(BR);
        } else {
            return null;
        }
    }
}
