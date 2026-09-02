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
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tree.Node;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

class MiniMessageImpl implements MiniMessage {
    static final UnaryOperator<String> DEFAULT_NO_OP = UnaryOperator.identity();
    static final UnaryOperator<Component> DEFAULT_COMPACTING_METHOD;
    static {
        UnaryOperator<Component> defaultCompacting = UnaryOperator.identity();
        try {
            defaultCompacting = Component::compact;
        } catch (Throwable ignored) {
        }
        DEFAULT_COMPACTING_METHOD = defaultCompacting;
    }

    private MiniMessageParser parser;
    private boolean strict;
    private boolean emitVirtuals;
    private @Nullable Consumer<String> debugOutput;
    private UnaryOperator<String> preProcessor;
    private UnaryOperator<Component> postProcessor;

    MiniMessageImpl(MiniMessageParser parser, boolean strict, boolean emitVirtuals, @Nullable Consumer<String> debugOutput, UnaryOperator<String> preProcessor, UnaryOperator<Component> postProcessor) {
        this.parser = parser;
        this.strict = strict;
        this.emitVirtuals = emitVirtuals;
        this.debugOutput = debugOutput;
        this.preProcessor = preProcessor;
        this.postProcessor = postProcessor;
    }

    MiniMessageImpl(final TagResolver parser, final boolean strict, final boolean emitVirtuals, final @Nullable Consumer<String> debugOutput, final UnaryOperator<String> preProcessor, final UnaryOperator<Component> postProcessor) {
        this(new MiniMessageParser(parser), strict, emitVirtuals, debugOutput, preProcessor, postProcessor);
    }

    public MiniMessageParser parser() {
        return parser;
    }

    public void parser(MiniMessageParser parser) {
        this.parser = parser;
    }

    public boolean strict() {
        return strict;
    }

    public void strict(boolean strict) {
        this.strict = strict;
    }

    public boolean emitVirtuals() {
        return emitVirtuals;
    }

    public void emitVirtuals(boolean emitVirtuals) {
        this.emitVirtuals = emitVirtuals;
    }

    public @Nullable Consumer<String> debugOutput() {
        return debugOutput;
    }

    public void debugOutput(@Nullable Consumer<String> debugOutput) {
        this.debugOutput = debugOutput;
    }

    public UnaryOperator<String> preProcessor() {
        return preProcessor;
    }

    public void preProcessor(UnaryOperator<String> preProcessor) {
        this.preProcessor = preProcessor;
    }

    public UnaryOperator<Component> postProcessor() {
        return postProcessor;
    }

    public void postProcessor(UnaryOperator<Component> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public Component deserialize(final String input) {
        return this.parser.parseFormat(this.newContext(input, null, null));
    }

    @Override
    public Component deserialize(final String input, final Pointered target) {
        return this.parser.parseFormat(this.newContext(input, requireNonNull(target, "target"), null));
    }

    @Override
    public Component deserialize(final String input, final TagResolver tagResolver) {
        return this.parser.parseFormat(this.newContext(input, null, requireNonNull(tagResolver, "tagResolver")));
    }

    @Override
    public Component deserialize(final String input, final Pointered target, final TagResolver tagResolver) {
        return this.parser.parseFormat(this.newContext(input, requireNonNull(target, "target"), requireNonNull(tagResolver, "tagResolver")));
    }

    @Override
    public Node.Root deserializeToTree(final String input) {
        return this.parser.parseToTree(this.newContext(input, null, null));
    }

    @Override
    public Node.Root deserializeToTree(final String input, final Pointered target) {
        return this.parser.parseToTree(this.newContext(input, requireNonNull(target, "target"), null));
    }

    @Override
    public Node.Root deserializeToTree(final String input, final TagResolver tagResolver) {
        return this.parser.parseToTree(this.newContext(input, null, requireNonNull(tagResolver, "tagResolver")));
    }

    @Override
    public Node.Root deserializeToTree(final String input, final Pointered target, final TagResolver tagResolver) {
        return this.parser.parseToTree(this.newContext(input, requireNonNull(target, "target"), requireNonNull(tagResolver, "tagResolver")));
    }

    @Override
    public String serialize(final Component component) {
        return MiniMessageSerializer.serialize(component, this.serialResolver(null), this.strict);
    }

    @Override
    public String escapeTags(final String input) {
        return this.parser.escapeTokens(this.newContext(input, null, null));
    }

    @Override
    public String escapeTags(final String input, final TagResolver tagResolver) {
        return this.parser.escapeTokens(this.newContext(input, null, tagResolver));
    }

    @Override
    public String stripTags(final String input) {
        return this.parser.stripTokens(this.newContext(input, null, null));
    }

    @Override
    public String stripTags(final String input, final TagResolver tagResolver) {
        return this.parser.stripTokens(this.newContext(input, null, tagResolver));
    }

    @Override
    public TagResolver tags() {
        return this.parser.tagResolver();
    }

    private SerializableResolver serialResolver(final @Nullable TagResolver extraResolver) {
        TagResolver resolver = this.parser.tagResolver();
        if (extraResolver == null) {
            if (resolver instanceof SerializableResolver) {
                return (SerializableResolver) resolver;
            }
        } else {
            final TagResolver combined = TagResolver.resolver(resolver, extraResolver);
            if (combined instanceof SerializableResolver) {
                return (SerializableResolver) combined;
            }
        }

        return (SerializableResolver) TagResolver.empty();
    }

    private ContextImpl newContext(final String input, final @Nullable Pointered target, final @Nullable TagResolver resolver) {
        requireNonNull(input, "input");
        return new ContextImpl(this.strict, this.emitVirtuals, this.debugOutput, input, this, target, resolver, this.preProcessor, this.postProcessor);
    }

    // We cannot store these fields in MiniMessageImpl directly due to class initialisation issues.
    static final class Instances {
        static final MiniMessage INSTANCE = new MiniMessageImpl(TagResolver.standard(), false, true, null, DEFAULT_NO_OP, DEFAULT_COMPACTING_METHOD);
    }

    static final class BuilderImpl implements Builder {
        private TagResolver tagResolver = TagResolver.standard();
        private boolean strict = false;
        private boolean emitVirtuals = true;
        private @Nullable Consumer<String> debug = null;
        private UnaryOperator<Component> postProcessor = DEFAULT_COMPACTING_METHOD;
        private UnaryOperator<String> preProcessor = DEFAULT_NO_OP;

        BuilderImpl() {
        }

        @Override
        public Builder tags(final TagResolver tags) {
            this.tagResolver = requireNonNull(tags, "tags");
            return this;
        }

        @Override
        public Builder editTags(final Consumer<TagResolver.Builder> adder) {
            requireNonNull(adder, "adder");
            final TagResolver.Builder builder = TagResolver.builder().resolver(this.tagResolver);
            adder.accept(builder);
            this.tagResolver = builder.build();
            return this;
        }

        @Override
        public Builder strict(final boolean strict) {
            this.strict = strict;
            return this;
        }

        @Override
        public Builder emitVirtuals(final boolean emitVirtuals) {
            this.emitVirtuals = emitVirtuals;
            return this;
        }

        @Override
        public Builder debug(final @Nullable Consumer<String> debugOutput) {
            this.debug = debugOutput;
            return this;
        }

        @Override
        public Builder postProcessor(final UnaryOperator<Component> postProcessor) {
            this.postProcessor = requireNonNull(postProcessor, "postProcessor");
            return this;
        }

        @Override
        public Builder preProcessor(final UnaryOperator<String> preProcessor) {
            this.preProcessor = requireNonNull(preProcessor, "preProcessor");
            return this;
        }

        @Override
        public MiniMessage build() {
            return new MiniMessageImpl(this.tagResolver, this.strict, this.emitVirtuals, this.debug, this.preProcessor, this.postProcessor);
        }
    }
}
