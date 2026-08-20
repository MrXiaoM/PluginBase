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
package top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.resolver;

import top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.Tag;

import java.util.Map;

public class SingleResolver implements TagResolver.Single, MappableResolver {
    private String key;
    private Tag tag;

    public SingleResolver(String key, Tag tag) {
        this.key = key;
        this.tag = tag;
    }

    @Override
    public String key() {
        return key;
    }

    public void key(String key) {
        this.key = key;
    }

    @Override
    public Tag tag() {
        return tag;
    }

    public void tag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public boolean has(final String name) {
        return this.key.equals(name);
    }

    @Override
    public boolean contributeToMap(final Map<String, Tag> map) {
        map.put(this.key, this.tag);
        return true;
    }

    @Override
    public void contributeKnownNames(final java.util.function.BiConsumer<String, TagResolver> sink) {
        sink.accept(this.key, this);
    }

    @Override
    public boolean isExhaustivelyKnown() {
        return true;
    }

    @Override
    public boolean mayProducePreProcess() {
        return this.tag instanceof top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.PreProcess;
    }
}
