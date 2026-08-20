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
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.match;

import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.TagInternals;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.Token;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenParser;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenParser.TagProvider;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenType;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.TagPart;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.PreProcess;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenParser.SEPARATOR;
import static top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenParser.tokenize;

/**
 * A matched token consumer that produces a string and returns a copy of the string with {@link PreProcess} tags resolved.
 *
 * @since 4.10.0
 */
public final class StringResolvingMatchedTokenConsumer extends MatchedTokenConsumer<String> {
    private final StringBuilder builder;
    private final TagProvider tagProvider;

    /**
     * Creates a string resolving matched token consumer.
     *
     * @param input       the input
     * @param tagProvider the resolver for argument-less tags
     * @since 4.10.0
     */
    public StringResolvingMatchedTokenConsumer(
            final String input,
            final TagProvider tagProvider
    ) {
        super(input);
        this.builder = new StringBuilder(input.length());
        this.tagProvider = tagProvider;
    }

    @Override
    public void accept(final int start, final int end, final TokenType tokenType) {
        super.accept(start, end, tokenType);

        if (tokenType != TokenType.OPEN_TAG) {
            // just add it normally, we don't care about other tags
            this.builder.append(this.input, start, end);
        } else {
            // well, now we need to work out if it's a tag or a placeholder!
            final String match = this.input.substring(start, end);
            final String cleanup = this.input.substring(start + 1, end - 1);

            final int index = cleanup.indexOf(SEPARATOR);
            final String tag = index == -1 ? cleanup : cleanup.substring(0, index);

            // we might care if it's a valid tag!
            if (TagInternals.sanitizeAndCheckValidTagName(tag)) {
                final String sanitized = TokenParser.TagProvider.sanitizePlaceholderName(tag);
                if (!this.tagProvider.mayProducePreProcess(sanitized)) {
                    // no resolver can produce a pre-process tag for this name — pass through verbatim
                    // without spending any resolution work on it
                    this.builder.append(match);
                    return;
                }
                final List<Token> tokens = tokenize(match, false);
                final List<TagPart> parts = new ArrayList<>();
                final List<Token> childs = tokens.isEmpty() ? null : tokens.get(0).childTokens();
                if (childs != null) {
                    for (int i = 1; i < childs.size(); i++) {
                        parts.add(new TagPart(match, childs.get(i), this.tagProvider));
                    }
                }
                // we might care if it's a pre-process!
                final Tag replacement = this.tagProvider.resolve(sanitized, parts, tokens.get(0));

                if (replacement instanceof PreProcess) {
                    PreProcess preProcess = (PreProcess) replacement;
                    this.builder.append(Objects.requireNonNull(preProcess.value(), "PreProcess replacements cannot return null"));
                    return;
                }
            }

            // if we get here, the placeholder wasn't found or was null
            this.builder.append(match);
        }
    }

    @Override
    public String result() {
        return this.builder.toString();
    }
}
