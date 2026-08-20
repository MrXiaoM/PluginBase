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

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.ParsingExceptionImpl;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.Token;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenParser;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.TokenType;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.ElementNode;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.RootNode;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.TagNode;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.ValueNode;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Inserting;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Modifying;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.PrioritizedTagResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MiniMessageParser {
    private TagResolver tagResolver;

    public MiniMessageParser(TagResolver tagResolver) {
        this.tagResolver = tagResolver;
    }

    public TagResolver tagResolver() {
        return tagResolver;
    }

    public void tagResolver(TagResolver tagResolver) {
        this.tagResolver = tagResolver;
    }

    String escapeTokens(final ContextImpl context) {
        final StringBuilder sb = new StringBuilder(context.message().length());
        this.escapeTokens(sb, context);
        return sb.toString();
    }

    void escapeTokens(final StringBuilder sb, final ContextImpl context) {
        this.escapeTokens(sb, context.message(), context);
    }

    private void escapeTokens(final StringBuilder sb, final String richMessage, final ContextImpl context) {
        this.processTokens(sb, richMessage, context, (token, builder) -> {
            builder.append('\\').append(TokenParser.TAG_START);
            if (token.type() == TokenType.CLOSE_TAG) {
                builder.append(TokenParser.CLOSE_TAG);
            }
            final List<Token> childTokens = token.childTokens();
            for (int i = 0; i < childTokens.size(); i++) {
                if (i != 0) {
                    builder.append(TokenParser.SEPARATOR);
                }
                this.escapeTokens(builder, childTokens.get(i).get(richMessage).toString(), context); // todo: do we need to unwrap quotes on this?
            }
            builder.append(TokenParser.TAG_END);
        });
    }

    String stripTokens(final ContextImpl context) {
        final StringBuilder sb = new StringBuilder(context.message().length());
        this.processTokens(sb, context, (token, builder) -> {
        });
        return sb.toString();
    }

    private void processTokens(final StringBuilder sb, final ContextImpl context, final BiConsumer<Token, StringBuilder> tagHandler) {
        this.processTokens(sb, context.message(), context, tagHandler);
    }

    private void processTokens(final StringBuilder sb, final String richMessage, final ContextImpl context, final BiConsumer<Token, StringBuilder> tagHandler) {
        final TagResolver combinedResolver = this.combinedResolver(context);
        final List<Token> root = TokenParser.tokenize(richMessage, true);
        for (final Token token : root) {
            switch (token.type()) {
                case TEXT:
                    sb.append(richMessage, token.startIndex(), token.endIndex());
                    break;
                case OPEN_TAG:
                case CLOSE_TAG:
                case OPEN_CLOSE_TAG: {
                    // extract tag name
                    if (token.childTokens().isEmpty()) {
                        sb.append(richMessage, token.startIndex(), token.endIndex());
                        continue;
                    }
                    final String sanitized = TokenParser.TagProvider.sanitizePlaceholderName(token.childTokens().get(0).get(richMessage).toString());
                    if (combinedResolver.has(sanitized)) {
                        tagHandler.accept(token, sb);
                    } else {
                        sb.append(richMessage, token.startIndex(), token.endIndex());
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unsupported token type " + token.type());
            }
        }
    }

    private TagResolver combinedResolver(final ContextImpl context) {
        final TagResolver extra = context.extraTags();
        if (extra == null || extra == TagResolver.empty()) {
            return this.tagResolver;
        }
        // per-call tags take priority over the instance tags
        return new PrioritizedTagResolver(extra, this.tagResolver);
    }

    RootNode parseToTree(final ContextImpl context) {
        final TagResolver combinedResolver = this.combinedResolver(context);
        final String processedMessage = context.preProcessor().apply(context.message());
        final Consumer<String> debug = context.debugOutput();
        if (debug != null) {
            debug.accept("Beginning parsing message ");
            debug.accept(processedMessage);
            debug.accept("\n");
        }

        final TokenParser.TagProvider transformationFactory = new TokenParser.TagProvider() {
            @Override
            public boolean has(final String sanitizedName) {
                return combinedResolver.has(sanitizedName);
            }

            @Override
            public boolean anyPreProcess() {
                return combinedResolver.mayProducePreProcess();
            }

            @Override
            public boolean mayProducePreProcess(final String sanitizedName) {
                return combinedResolver.mayProducePreProcess(sanitizedName);
            }

            @Override
            public @Nullable Tag resolve(final String name, final List<? extends Tag.Argument> args, final @Nullable Token token) {
                try {
                    if (debug != null) {
                        debug.accept("Attempting to match node '");
                        debug.accept(name);
                        debug.accept("'");
                        if (token != null) {
                            debug.accept(" at column ");
                            debug.accept(String.valueOf(token.startIndex()));
                        }
                        debug.accept("\n");
                    }

                    final Tag transformation = combinedResolver.resolve(name, new ArgumentQueueImpl<>(context, args), context);

                    if (debug != null) {
                        if (transformation == null) {
                            debug.accept("Could not match node '");
                            debug.accept(name);
                            debug.accept("'\n");
                        } else {
                            debug.accept("Successfully matched node '");
                            debug.accept(name);
                            debug.accept("' to tag ");
                            debug.accept(transformation.getClass().getName());
                            debug.accept("\n");
                        }
                    }

                    return transformation;
                } catch (final ParsingException e) {
                    if (token != null && e instanceof ParsingExceptionImpl) {
                        ParsingExceptionImpl impl = (ParsingExceptionImpl) e;
                        if (impl.tokens().length == 0) {
                            impl.tokens(new Token[]{token});
                        }
                    }
                    if (debug != null) {
                        debug.accept("Could not match node '");
                        debug.accept(name);
                        debug.accept("' - ");
                        debug.accept(e.getMessage());
                        debug.accept("\n");
                    }
                    return null;
                }
            }
        };

        // the pre-process pass is only run when some resolver can actually produce pre-process tags
        final String preProcessed;
        if (transformationFactory.anyPreProcess()) {
            preProcessed = TokenParser.resolvePreProcessTags(processedMessage, transformationFactory);
        } else {
            preProcessed = processedMessage;
        }
        context.message(preProcessed);
        // Then, once MiniMessage placeholders have been inserted, we can do the real parse
        final RootNode root = TokenParser.parse(transformationFactory, preProcessed, processedMessage, context.strict());

        if (debug != null) {
            debug.accept("Text parsed into element tree:\n");
            debug.accept(root.toString());
        }

        return root;
    }

    Component parseFormat(final ContextImpl context) {
        final ElementNode root = this.parseToTree(context);
        return Objects.requireNonNull(context.postProcessor().apply(this.treeToComponent(root, context)), "Post-processor must not return null");
    }

    Component treeToComponent(final ElementNode node, final ContextImpl context) {
        Component comp = Component.empty();
        Tag tag = null;
        if (node instanceof ValueNode) {
            ValueNode valueNode = (ValueNode) node;
            comp = Component.text(valueNode.value());
        } else if (node instanceof TagNode) {
            TagNode tagNode = (TagNode) node;

            tag = tagNode.tag();

            // special case for gradient and stuff
            if (tag instanceof Modifying) {
                Modifying modTransformation = (Modifying) tag;

                // first walk the tree
                this.visitModifying(modTransformation, tagNode, 0);
                modTransformation.postVisit();
            }

            if (tag instanceof Inserting) {
                comp = ((Inserting) tag).value();
            }
        }

        if (!node.unsafeChildren().isEmpty()) {
            final List<Component> children = new ArrayList<>(comp.children().size() + node.children().size());
            children.addAll(comp.children());
            for (final ElementNode child : node.unsafeChildren()) {
                children.add(this.treeToComponent(child, context));
            }
            comp = comp.children(children);
        }

        // special case for gradient and stuff
        if (tag instanceof Modifying) {
            comp = this.handleModifying((Modifying) tag, comp, 0);
        }

        final Consumer<String> debug = context.debugOutput();
        if (debug != null) {
            debug.accept("==========\ntreeToComponent \n");
            debug.accept(node.toString());
            debug.accept("\n");
            debug.accept(comp.toString());
            debug.accept("\n==========\n");
        }

        return comp;
    }

    private void visitModifying(final Modifying modTransformation, final ElementNode node, final int depth) {
        modTransformation.visit(node, depth);
        for (final ElementNode child : node.unsafeChildren()) {
            this.visitModifying(modTransformation, child, depth + 1);
        }
    }

    private Component handleModifying(final Modifying modTransformation, final Component current, final int depth) {
        Component newComp = modTransformation.apply(current, depth);
        for (final Component child : current.children()) {
            newComp = newComp.append(this.handleModifying(modTransformation, child, depth + 1));
        }
        return newComp;
    }
}
