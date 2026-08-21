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
import net.kyori.adventure.text.flattener.ComponentFlattener;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;

/**
 * Shared support for tag arguments that may contain nested tags: the argument value is
 * deserialized as MiniMessage using the current parse context and flattened to a plain
 * string. For example, {@code <click:open_url:'<url>'>} resolves the {@code url}
 * placeholder and uses its plain text as the click payload.
 *
 * <p>Note that nested tags must be quoted ({@code '...'}) — an unquoted {@code <} inside
 * a tag restarts tokenization, as everywhere in the MiniMessage grammar.</p>
 */
final class NestedArguments {
    private NestedArguments() {
    }

    /**
     * Resolves nested tags in {@code value} using {@code ctx}, returning plain text.
     * Values without any tag start character are returned as-is, at zero extra cost.
     */
    static String resolvePlain(final String value, final Context ctx) {
        if (value.indexOf('<') == -1) {
            return value;
        }
        final Component parsed = ctx.deserialize(value);
        final StringBuilder sb = new StringBuilder();
        ComponentFlattener.textOnly().flatten(parsed, sb::append);
        return sb.toString();
    }
}
