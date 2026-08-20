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
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message;

import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tree.Node;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * A parsed string representation of the Adventure {@link Component} API — an
 * optimized reimplementation of the MiniMessage language.
 *
 * <p>Resolver lookups are compiled into dispatch tables at build time, so parse
 * performance stays flat no matter how many tag resolvers are registered.</p>
 */
public interface MiniMessage {
    /**
     * Get a shared instance with all standard tags.
     *
     * @return a shared instance
     */
    static MiniMessage miniMessage() {
        return MiniMessageImpl.Instances.INSTANCE;
    }

    /**
     * Create a new builder.
     *
     * @return a builder
     */
    static Builder builder() {
        return new MiniMessageImpl.BuilderImpl();
    }

    /**
     * Deserializes a string into a component, parsing any tags found.
     *
     * @param input the input string
     * @return the output component
     */
    Component deserialize(final String input);

    /**
     * Deserializes a string into a component, parsing any tags found, with a target for context-dependent tags.
     *
     * @param input  the input string
     * @param target the target of this deserialization
     * @return the output component
     */
    Component deserialize(final String input, final Pointered target);

    /**
     * Deserializes a string into a component, parsing any tags found, with a tag resolver for extra tags.
     *
     * @param input       the input string
     * @param tagResolver the tag resolver for any extra tags to handle
     * @return the output component
     */
    Component deserialize(final String input, final TagResolver tagResolver);

    /**
     * Deserializes a string into a component, parsing any tags found, with a target and a tag resolver for extra tags.
     *
     * @param input       the input string
     * @param target      the target of this deserialization
     * @param tagResolver the tag resolver for any extra tags to handle
     * @return the output component
     */
    Component deserialize(final String input, final Pointered target, final TagResolver tagResolver);

    /**
     * Deserializes a string into a component, parsing any tags found, with a tag resolver for extra tags.
     *
     * @param input        the input string
     * @param tagResolvers the tag resolvers for any extra tags to handle
     * @return the output component
     */
    default Component deserialize(final String input, final TagResolver... tagResolvers) {
        return this.deserialize(input, TagResolver.resolver(tagResolvers));
    }

    /**
     * Deserializes a string into a component, parsing any tags found, with a target and a tag resolver for extra tags.
     *
     * @param input        the input string
     * @param target       the target of this deserialization
     * @param tagResolvers the tag resolvers for any extra tags to handle
     * @return the output component
     */
    default Component deserialize(final String input, final Pointered target, final TagResolver... tagResolvers) {
        return this.deserialize(input, target, TagResolver.resolver(tagResolvers));
    }

    /**
     * Deserializes a string into a node tree, parsing any tags found.
     *
     * @param input the input string
     * @return the root of the resulting tree
     */
    Node.Root deserializeToTree(final String input);

    /**
     * Deserializes a string into a node tree, parsing any tags found, with a tag resolver for extra tags.
     *
     * @param input       the input string
     * @param tagResolver the tag resolver for any extra tags to handle
     * @return the root of the resulting tree
     */
    Node.Root deserializeToTree(final String input, final TagResolver tagResolver);

    /**
     * Deserializes a string into a node tree, parsing any tags found, with a target for context-dependent tags.
     *
     * @param input  the input string
     * @param target the target of this deserialization
     * @return the root of the resulting tree
     */
    Node.Root deserializeToTree(final String input, final Pointered target);

    /**
     * Deserializes a string into a node tree, parsing any tags found, with a target and a tag resolver for extra tags.
     *
     * @param input       the input string
     * @param target      the target of this deserialization
     * @param tagResolver the tag resolver for any extra tags to handle
     * @return the root of the resulting tree
     */
    Node.Root deserializeToTree(final String input, final Pointered target, final TagResolver tagResolver);

    /**
     * Serializes a component into a MiniMessage string, using the tags known to this instance.
     *
     * @param component the component to serialize
     * @return the MiniMessage string
     */
    String serialize(final Component component);

    /**
     * Escapes all known tags in the input message, so that they are ignored in deserialization.
     *
     * @param input the input message
     * @return the output message
     */
    String escapeTags(final String input);

    /**
     * Escapes all known tags in the input message, so that they are ignored in deserialization.
     *
     * @param input       the input message
     * @param tagResolver the tag resolver for additional tags to handle
     * @return the output message
     */
    String escapeTags(final String input, final TagResolver tagResolver);

    /**
     * Escapes all known tags in the input message, so that they are ignored in deserialization.
     *
     * @param input        the input message
     * @param tagResolvers the tag resolvers for additional tags to handle
     * @return the output message
     */
    default String escapeTags(final String input, final TagResolver... tagResolvers) {
        return this.escapeTags(input, TagResolver.resolver(tagResolvers));
    }

    /**
     * Removes all known tags in the input message, so that their text contents remain but tags are removed.
     *
     * @param input the input message
     * @return the output message
     */
    String stripTags(final String input);

    /**
     * Removes all known tags in the input message, so that their text contents remain but tags are removed.
     *
     * @param input       the input message
     * @param tagResolver the tag resolver for additional tags to handle
     * @return the output message
     */
    String stripTags(final String input, final TagResolver tagResolver);

    /**
     * Removes all known tags in the input message, so that their text contents remain but tags are removed.
     *
     * @param input        the input message
     * @param tagResolvers the tag resolvers for additional tags to handle
     * @return the output message
     */
    default String stripTags(final String input, final TagResolver... tagResolvers) {
        return this.stripTags(input, TagResolver.resolver(tagResolvers));
    }

    /**
     * Gets the tag resolver used by this instance of MiniMessage.
     *
     * @return the tag resolver
     */
    TagResolver tags();

    /**
     * A builder for MiniMessage instances.
     */
    interface Builder {
        /**
         * Set the tag resolver to be used by the resulting MiniMessage instance.
         *
         * @param tags the new tag resolver
         * @return this builder
         */
        Builder tags(final TagResolver tags);

        /**
         * Allows the current tag resolver to be edited.
         *
         * @param adder the edit action
         * @return this builder
         */
        Builder editTags(final Consumer<TagResolver.Builder> adder);

        /**
         * Whether to parse in strict mode, where tags must be explicitly closed.
         *
         * @param strict the strict mode setting
         * @return this builder
         */
        Builder strict(final boolean strict);

        /**
         * Whether color-changing tags (gradient, rainbow, transition) emit lazy virtual
         * components rather than eagerly colored text.
         *
         * @param emitVirtuals the virtual emission setting
         * @return this builder
         */
        Builder emitVirtuals(final boolean emitVirtuals);

        /**
         * Set the debug output consumer.
         *
         * @param debugOutput where to print debug output, or {@code null} to disable
         * @return this builder
         */
        Builder debug(final @Nullable Consumer<String> debugOutput);

        /**
         * Set a post-processor for resulting components.
         *
         * @param postProcessor the post-processor
         * @return this builder
         */
        Builder postProcessor(final UnaryOperator<Component> postProcessor);

        /**
         * Set a pre-processor for input strings, applied before parsing.
         *
         * @param preProcessor the pre-processor
         * @return this builder
         */
        Builder preProcessor(final UnaryOperator<String> preProcessor);

        /**
         * Build the new MiniMessage instance.
         *
         * @return the MiniMessage instance
         */
        MiniMessage build();
    }
}
