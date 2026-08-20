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
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;

import static java.util.Objects.requireNonNull;

/**
 * Tag types distributed with this MiniMessage implementation.
 *
 * <p>All built-in types are included in the default tag resolver.
 * This set intentionally excludes the pride tag.</p>
 */
public final class StandardTags {

    private static final TagResolver ALL = TagResolver.builder()
            .resolvers(
                    () -> HoverTag.RESOLVER,
                    () -> ClickTag.RESOLVER,
                    () -> ColorTagResolver.INSTANCE,
                    () -> KeybindTag.RESOLVER,
                    () -> TranslatableTag.RESOLVER,
                    () -> TranslatableFallbackTag.RESOLVER,
                    () -> InsertionTag.RESOLVER,
                    () -> FontTag.RESOLVER,
                    () -> DecorationTag.RESOLVER,
                    () -> GradientTag.RESOLVER,
                    () -> RainbowTag.RESOLVER,
                    () -> ResetTag.RESOLVER,
                    () -> NewlineTag.RESOLVER,
                    () -> TransitionTag.RESOLVER,
                    () -> SelectorTag.RESOLVER,
                    () -> ScoreTag.RESOLVER,
                    () -> NbtTag.RESOLVER,
                    () -> ShadowColorTag.RESOLVER,
                    () -> SpriteTag.RESOLVER,
                    () -> SequentialHeadTag.RESOLVER
            )
            .build();
    private static final TagResolver NON_INTERACTABLE = TagResolver.builder()
            .resolvers(
                    () -> ColorTagResolver.INSTANCE,
                    () -> KeybindTag.RESOLVER,
                    () -> TranslatableTag.RESOLVER,
                    () -> TranslatableFallbackTag.RESOLVER,
                    () -> FontTag.RESOLVER,
                    () -> DecorationTag.RESOLVER,
                    () -> GradientTag.RESOLVER,
                    () -> RainbowTag.RESOLVER,
                    () -> ResetTag.RESOLVER,
                    () -> NewlineTag.RESOLVER,
                    () -> TransitionTag.RESOLVER,
                    () -> SelectorTag.RESOLVER,
                    () -> ScoreTag.RESOLVER,
                    () -> NbtTag.RESOLVER,
                    () -> ShadowColorTag.RESOLVER,
                    () -> SpriteTag.RESOLVER,
                    () -> SequentialHeadTag.RESOLVER
            )
            .build();
    private static final TagResolver FORMATTED_TEXT = TagResolver.builder()
            .resolvers(
                    () -> ColorTagResolver.INSTANCE,
                    () -> FontTag.RESOLVER,
                    () -> DecorationTag.RESOLVER,
                    () -> GradientTag.RESOLVER,
                    () -> RainbowTag.RESOLVER,
                    () -> NewlineTag.RESOLVER,
                    () -> TransitionTag.RESOLVER,
                    () -> ShadowColorTag.RESOLVER
            )
            .build();

    private StandardTags() {
    }

    /**
     * Get a resolver for a specific text decoration.
     *
     * @param decoration the decoration to have a tag for
     * @return a resolver for a certain decoration's tags
     */
    public static TagResolver decorations(final TextDecoration decoration) {
        return requireNonNull(DecorationTag.RESOLVERS.get(decoration), "No resolver found for decoration (this should not be possible?)");
    }

    /**
     * Get a resolver for all decoration tags.
     *
     * @return a resolver for all decoration tags
     */
    public static TagResolver decorations() {
        return DecorationTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value ColorTagResolver#COLOR} tags.
     *
     * @return a resolver for the {@value ColorTagResolver#COLOR} tags
     */
    public static TagResolver color() {
        return ColorTagResolver.INSTANCE;
    }

    /**
     * Get a resolver for the {@value HoverTag#HOVER} tag.
     *
     * @return a resolver for the {@value HoverTag#HOVER} tag
     */
    public static TagResolver hoverEvent() {
        return HoverTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value ClickTag#CLICK} tag.
     *
     * @return a resolver for the {@value ClickTag#CLICK} tag
     */
    public static TagResolver clickEvent() {
        return ClickTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value KeybindTag#KEYBIND} tag.
     *
     * @return a resolver for the {@value KeybindTag#KEYBIND} tag
     */
    public static TagResolver keybind() {
        return KeybindTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value SequentialHeadTag#HEAD} tag.
     *
     * @return a resolver for the {@value SequentialHeadTag#HEAD} tag.
     */
    public static TagResolver sequentialHead() {
        return SequentialHeadTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value TranslatableTag#TRANSLATE} tag.
     *
     * @return a resolver for the {@value TranslatableTag#TRANSLATE} tag
     */
    public static TagResolver translatable() {
        return TranslatableTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value TranslatableFallbackTag#TRANSLATE_OR} tag.
     *
     * @return a resolver for the {@value TranslatableFallbackTag#TRANSLATE_OR} tag
     */
    public static TagResolver translatableFallback() {
        return TranslatableFallbackTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value InsertionTag#INSERTION} tag.
     *
     * @return a resolver for the {@value InsertionTag#INSERTION} tag
     */
    public static TagResolver insertion() {
        return InsertionTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value FontTag#FONT} tag.
     *
     * @return a resolver for the {@value FontTag#FONT} tag
     */
    public static TagResolver font() {
        return FontTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value GradientTag#GRADIENT} tag.
     *
     * @return a resolver for the {@value GradientTag#GRADIENT} tag
     */
    public static TagResolver gradient() {
        return GradientTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value RainbowTag#RAINBOW} tag.
     *
     * @return a resolver for the {@value RainbowTag#RAINBOW} tag
     */
    public static TagResolver rainbow() {
        return RainbowTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value TransitionTag#TRANSITION} tag.
     *
     * @return a resolver for the {@value TransitionTag#TRANSITION} tag
     */
    public static TagResolver transition() {
        return TransitionTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value ResetTag#RESET} tag.
     *
     * @return a resolver for the {@value ResetTag#RESET} tag.
     */
    public static TagResolver reset() {
        return ResetTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value NewlineTag#NEWLINE} tag.
     *
     * @return a resolver for the {@value NewlineTag#NEWLINE} tag.
     */
    public static TagResolver newline() {
        return NewlineTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value SelectorTag#SELECTOR} tag.
     *
     * @return a resolver for the {@value SelectorTag#SELECTOR} tag
     */
    public static TagResolver selector() {
        return SelectorTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value ScoreTag#SCORE} tag.
     *
     * @return a resolver for the {@value ScoreTag#SCORE} tag
     */
    public static TagResolver score() {
        return ScoreTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value NbtTag#NBT} tag.
     *
     * @return a resolver for the {@value NbtTag#NBT} tag.
     */
    public static TagResolver nbt() {
        return NbtTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value ShadowColorTag#SHADOW_COLOR} tags.
     *
     * @return a resolver for the {@value ShadowColorTag#SHADOW_COLOR} tags
     */
    public static TagResolver shadowColor() {
        return ShadowColorTag.RESOLVER;
    }

    /**
     * Get a resolver for the {@value SpriteTag#SPRITE} tag.
     *
     * @return a resolver for the {@value SpriteTag#SPRITE} tag.
     */
    public static TagResolver sprite() {
        return SpriteTag.RESOLVER;
    }

    /**
     * Get a resolver that handles all default standard tags.
     *
     * @return the resolver for built-in tags
     */
    public static TagResolver defaults() {
        return ALL;
    }

    /**
     * Get a resolver that handles all standard tags that do not require interaction with the viewer.
     *
     * @return the non-interactable resolver
     */
    public static TagResolver nonInteractable() {
        return NON_INTERACTABLE;
    }

    /**
     * Get a resolver that handles all standard tags that only format text.
     *
     * @return the formatted-text resolver
     */
    public static TagResolver formattedText() {
        return FORMATTED_TEXT;
    }
}
