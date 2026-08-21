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

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.TagNode;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.parser.node.ValueNode;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.Emitable;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.TokenEmitter;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Inserting;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Modifying;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.virtual.VirtualOperation;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tree.Node;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

/**
 * A transformation that applies a colour change.
 *
 * <p>The lifecycle is:</p>
 *
 * <ol>
 * <li>init()</li>
 * <li>(color()? advanceColor())*</li>
 * </ol>
 *
 * @since 4.10.0
 */
abstract class AbstractColorChangingTag implements Modifying {
    private static final ComponentFlattener LENGTH_CALCULATOR = ComponentFlattener.builder()
            .mapper(TextComponent.class, TextComponent::content)
            .unknownMapper(x -> "_") // every unknown component gets a single colour
            .build();
    private static final VirtualOperation virtual = VirtualOperation.create();
    private final boolean emitVirtuals;
    private boolean visited;
    private int size = 0;
    private int disableApplyingColorDepth = -1;

    AbstractColorChangingTag(final Context ctx) {
        this.emitVirtuals = ctx.emitVirtuals();
    }

    static @Nullable Emitable claimComponent(final Component comp) {
        return virtual.claimComponent(comp);
    }

    protected final int size() {
        return this.size;
    }

    @Override
    public final void visit(final Node current, final int depth) {
        if (this.visited) {
            throw new IllegalStateException("Color changing tag instances cannot be re-used, return a new one for each resolve");
        }

        if (current instanceof ValueNode) {
            ValueNode valueNode = (ValueNode) current;
            final String value = valueNode.value();
            this.size += value.codePointCount(0, value.length());
        } else if (current instanceof TagNode) {
            TagNode tag = (TagNode) current;
            if (tag.tag() instanceof Inserting) {
                // ComponentTransformation.apply() returns the value of the component placeholder
                LENGTH_CALCULATOR.flatten(((Inserting) tag.tag()).value(), s -> this.size += s.codePointCount(0, s.length()));
            }
        }
    }

    @Override
    public final void postVisit() {
        // init
        this.visited = true;
        this.init();
    }

    @Override
    public final Component apply(final Component current, final int depth) {
        if (this.emitVirtuals && depth == 0) {
            // capture state into a virtual component, no other logic is needed in normal MM handling
            return virtual.createVirtualTagInfoHolder(this.preserveData(), current);
        }

        if ((this.disableApplyingColorDepth != -1 && depth > this.disableApplyingColorDepth) || current.style().color() != null) {
            if (this.disableApplyingColorDepth == -1 || depth < this.disableApplyingColorDepth) {
                this.disableApplyingColorDepth = depth;
            }
            // This component has its own color applied, which overrides ours
            // We still want to keep track of where we are though if this is text
            if (current instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) current;
                this.skipColorForLengthOf(textComponent.content());
            }
            return current.children(new ArrayList<>());
        }

        this.disableApplyingColorDepth = -1;
        String virtualContent = virtual.content(current);
        if (virtualContent != null) {
            // this component has its own information, so we can't rainbowify direct content -- we can process children tho
            // basically treat as if it's a non-text component
            this.skipColorForLengthOf(virtualContent);
            return current.children(new ArrayList<>());
        } else if (current instanceof TextComponent && !((TextComponent) current).content().isEmpty()) {
            TextComponent textComponent = (TextComponent) current;
            final String content = textComponent.content();

            final TextComponent.Builder parent = Component.text();

            // apply
            final int[] holder = new int[1];
            for (final PrimitiveIterator.OfInt it = content.codePoints().iterator(); it.hasNext(); ) {
                holder[0] = it.nextInt();
                final Component comp = Component.text(new String(holder, 0, 1), current.style().color(this.color()));
                this.advanceColor();
                parent.append(comp);
            }

            return parent.build();
        } else if (!(current instanceof TextComponent)) {
            final Component ret = current.children(new ArrayList<>()).colorIfAbsent(this.color());
            this.advanceColor();
            return ret;
        }

        return Component.empty().mergeStyle(current);
    }

    // The lifecycle

    private void skipColorForLengthOf(final String content) {
        final int len = content.codePointCount(0, content.length());
        for (int i = 0; i < len; i++) {
            // increment our color index
            this.advanceColor();
        }
    }

    protected abstract void init();

    /**
     * Advance the active color.
     */
    protected abstract void advanceColor();

    /**
     * Get the current color, without side-effects.
     *
     * @return the current color
     * @since 4.10.0
     */
    protected abstract TextColor color();

    // misc

    /**
     * Return an emitable that will accurately reserialize the provided input data.
     *
     * @return the emitable for this tag
     * @since 4.18.0
     */
    protected abstract Consumer<TokenEmitter> preserveData();

    @Override
    public abstract boolean equals(final @Nullable Object other);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
