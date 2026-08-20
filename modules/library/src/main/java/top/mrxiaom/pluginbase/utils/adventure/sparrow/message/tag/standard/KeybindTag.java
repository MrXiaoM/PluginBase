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
import net.kyori.adventure.text.KeybindComponent;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.ArgumentQueue;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

/**
 * A transformation that inserts a key binding component.
 *
 * @since 4.10.0
 */
public final class KeybindTag {
    public static final String KEYBIND = "key";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(KeybindTag.KEYBIND, KeybindTag::create, KeybindTag::emit);

    private KeybindTag() {
    }

    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        return Tag.inserting(Component.keybind(args.popOr("A keybind id is required").value()));
    }

    static @Nullable Emitable emit(final Component component) {
        if (!(component instanceof KeybindComponent)) return null;
        KeybindComponent keybindComponent = (KeybindComponent) component;

        final String key = keybindComponent.keybind();

        return emit -> emit.tag(KEYBIND).argument(key);
    }
}
