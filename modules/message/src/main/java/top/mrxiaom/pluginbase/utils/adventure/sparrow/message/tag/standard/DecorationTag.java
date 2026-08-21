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

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.StyleClaim;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.TokenEmitter;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.ArgumentQueue;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A transformation that applies any {@link TextDecoration}.
 *
 * @since 4.10.0
 */
public final class DecorationTag {
    public static final String REVERT = "!";
    // vanilla decoration
    private static final String B = "b";
    private static final String I = "i";
    private static final String EM = "em";
    private static final String OBF = "obf";
    private static final String ST = "st";
    private static final String U = "u";
    static final Map<TextDecoration, TagResolver> RESOLVERS = Stream.of(
                    resolvers(TextDecoration.OBFUSCATED, OBF),
                    resolvers(TextDecoration.BOLD, B),
                    resolvers(TextDecoration.STRIKETHROUGH, ST),
                    resolvers(TextDecoration.UNDERLINED, U),
                    resolvers(TextDecoration.ITALIC, EM, I)
            )
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    ent -> ent.getValue().collect(TagResolver.toTagResolver()),
                    (l, r) -> TagResolver.builder().resolver(l).resolver(r).build(),
                    LinkedHashMap::new
            ));
    public static final TagResolver RESOLVER = TagResolver.resolver(RESOLVERS.values());

    private DecorationTag() {
    }

    // create resolvers for canonical + configured alternates
    static Map.Entry<TextDecoration, Stream<TagResolver>> resolvers(final TextDecoration decoration, final @Nullable String shortName, final String... secondaryAliases) {
        final String canonicalName = TextDecoration.NAMES.keyOrThrow(decoration);
        final Set<String> names = new HashSet<>();
        names.add(canonicalName);
        if (shortName != null) names.add(shortName);
        Collections.addAll(names, secondaryAliases);

        return new AbstractMap.SimpleImmutableEntry<>(decoration, Stream.concat(
                Stream.of(SerializableResolver.claimingStyle(
                        names,
                        (args, ctx) -> DecorationTag.create(decoration, args, ctx),
                        claim(decoration, (state, emitter) -> emit(canonicalName, shortName == null ? canonicalName : shortName, state, emitter))
                )),
                names.stream().map(name -> TagResolver.resolver(DecorationTag.REVERT + name, DecorationTag.createNegated(decoration)))
        ));
    }

    static Tag create(final TextDecoration toApply, final ArgumentQueue args, final Context ctx) {
        final boolean flag = !args.hasNext() || !args.pop().isFalse();

        return Tag.styling(toApply.withState(flag));
    }

    static Tag createNegated(final TextDecoration toApply) {
        return Tag.styling(toApply.withState(false));
    }

    static StyleClaim<TextDecoration.State> claim(final TextDecoration decoration, final BiConsumer<TextDecoration.State, TokenEmitter> emitable) {
        requireNonNull(decoration, "decoration");
        return StyleClaim.claim(
                "decoration_" + TextDecoration.NAMES.key(decoration),
                style -> style.decoration(decoration),
                state -> state != TextDecoration.State.NOT_SET,
                emitable
        );
    }

    static void emit(final String longName, final String shortName, final TextDecoration.State state, final TokenEmitter emitter) {
        if (state == State.FALSE) {
            emitter.tag(REVERT + longName);
        } else {
            emitter.tag(longName);
        }
    }
}
