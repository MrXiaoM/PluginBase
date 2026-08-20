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
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal;

import top.mrxiaom.pluginbase.utils.adventure.TagPattern;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility class for tag naming.
 *
 * @since 4.10.0
 */
@ApiStatus.Internal
public final class TagInternals {
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile(TagPattern.TAG_NAME_REGEX);

    private TagInternals() {
    }

    /**
     * Checks if a tag name matches the pattern for allowed tag names. If it does not, then
     * this method will throw an {@link IllegalArgumentException}
     *
     * @param tagName the name of the tag
     * @since 4.10.0
     */
    public static void assertValidTagName(@TagPattern final String tagName) {
        if (!TAG_NAME_PATTERN.matcher(Objects.requireNonNull(tagName)).matches()) {
            throw new IllegalArgumentException("Tag name must match pattern " + TAG_NAME_PATTERN.pattern() + ", was " + tagName);
        }
    }

    /**
     * Checks if a tag name matches the pattern for allowed tag names, first sanitizing it
     * by converting the tag name to lowercase. Returns a boolean representing the validity
     *
     * <p>This is on the hot parse path (every tag candidate), so it is a hand-rolled
     * equivalent of {@code [!?#]?[a-z0-9_-]*} on the lower-cased name — no regex,
     * no Matcher or String allocation.</p>
     *
     * @param tagName the name of the tag
     * @return validity of this tag when sanitized
     * @since 4.10.1
     */
    public static boolean sanitizeAndCheckValidTagName(@TagPattern final String tagName) {
        Objects.requireNonNull(tagName);
        final int length = tagName.length();
        int i = 0;
        if (length > 0) {
            final char first = tagName.charAt(0);
            if (first == '!' || first == '?' || first == '#') {
                i = 1;
            }
        }
        for (; i < length; i++) {
            final char c = tagName.charAt(i);
            // accepting A-Z directly is equivalent to lower-casing before matching
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a tag name matches the pattern for allowed tag names, first sanitizing it
     * by converting the tag name to lowercase. If it does not match the pattern, then this
     * method will throw an {@link IllegalArgumentException}
     *
     * @param tagName the name of the tag
     * @since 4.10.0
     */
    public static void sanitizeAndAssertValidTagName(@TagPattern final String tagName) {
        assertValidTagName(Objects.requireNonNull(tagName).toLowerCase(Locale.ROOT));
    }
}
