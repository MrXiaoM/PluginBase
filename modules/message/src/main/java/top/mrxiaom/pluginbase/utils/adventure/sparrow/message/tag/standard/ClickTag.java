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

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.Context;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.ParsingException;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.QuotingOverride;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.SerializableResolver;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.internal.serializer.StyleClaim;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.ArgumentQueue;
import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver.TagResolver;

/**
 * Click events.
 *
 * @since 4.10.0
 */
public class ClickTag {
    public static final String CLICK = "click";

    public static final TagResolver RESOLVER = SerializableResolver.claimingStyle(
            CLICK,
            ClickTag::create,
            StyleClaim.claim(CLICK, Style::clickEvent, (event, emitter) -> {
                String actionName = ClickEvent.Action.NAMES.keyOrThrow(event.action());
                try {
                    final ClickEvent.Payload payload = event.payload();
                    final String argument;
                    if (payload instanceof ClickEvent.Payload.Custom) {
                        argument = ((ClickEvent.Payload.Custom) payload).key().asString();
                    } else if (payload instanceof ClickEvent.Payload.Dialog) {
                        throw new UnsupportedOperationException("show_dialog click events cannot be serialized by MiniMessage yet");
                    } else if (payload instanceof ClickEvent.Payload.Int) {
                        argument = String.valueOf(((ClickEvent.Payload.Int) payload).integer());
                    } else if (payload instanceof ClickEvent.Payload.Text) {
                        argument = ((ClickEvent.Payload.Text) payload).value();
                    } else {
                        throw new UnsupportedOperationException("Invalid payload type " + payload.getClass().getName());
                    }
                    emitter.tag(CLICK)
                            .argument(actionName)
                            .argument(argument, QuotingOverride.QUOTED);

                    if (payload instanceof ClickEvent.Payload.Custom) {
                        final BinaryTagHolder nbt = ((ClickEvent.Payload.Custom) payload).nbt();
                        if (nbt != null) {
                            emitter.argument(nbt.string());
                        }
                    }
                } catch (LinkageError ex) {
                    // 适配低版本 adventure 的过时用法
                    emitter.tag(CLICK)
                            .argument(actionName)
                            .argument(event.value(), QuotingOverride.QUOTED);
                }
            })
    );

    @SuppressWarnings("PatternValidation") // We check the pattern of the key with a catch.
    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        final String actionName = args.popOr(() -> "A click tag requires an action of one of " + ClickEvent.Action.NAMES.keys()).lowerValue();

        final ClickEvent event;
        switch (actionName) {
            case "change_page": {
                event = ClickEvent.changePage(
                        args
                                .popOr("'change_page' click event requires a page argument")
                                .asInt()
                                .orElseThrow(() -> ctx.newException("'change_page' click event requires an integer page argument", args)));
                break;
            }
            case "custom": {
                final String keyString = NestedArguments.resolvePlain(args.popOr("'custom' click event requires a key argument").value(), ctx);
                final Key key;
                try {
                    key = Key.key(keyString);
                } catch (final InvalidKeyException ex) {
                    throw ctx.newException("'custom' click event requires a valid key argument", ex, args);
                }

                final String nbt;
                if (args.hasNext()) {
                    nbt = NestedArguments.resolvePlain(args.pop().value(), ctx);
                } else {
                    nbt = null;
                }

                event = ClickEvent.custom(key, nbt == null ? null : BinaryTagHolder.binaryTagHolder(nbt));
                break;
            }
            case "show_dialog":
                throw ctx.newException("'show_dialog' click events are not supported in MiniMessage yet");
            case "open_url": {
                event = ClickEvent.openUrl(textCarrier("open_url", args, ctx));
                break;
            }
            case "open_file": {
                event = ClickEvent.openFile(textCarrier("open_file", args, ctx));
                break;
            }
            case "run_command": {
                event = ClickEvent.runCommand(textCarrier("run_command", args, ctx));
                break;
            }
            case "suggest_command": {
                event = ClickEvent.suggestCommand(textCarrier("suggest_command", args, ctx));
                break;
            }
            case "copy_to_clipboard": {
                event = ClickEvent.copyToClipboard(textCarrier("copy_to_clipboard", args, ctx));
                break;
            }
            default:
                throw ctx.newException("Unknown click event action '" + actionName + "'", args);
        }

        return Tag.styling(event);
    }

    static String textCarrier(final String textCarrier, final ArgumentQueue args, final Context ctx) {
        return NestedArguments.resolvePlain(args.popOr("'" + textCarrier + "' click events require a value").value(), ctx);
    }
}
