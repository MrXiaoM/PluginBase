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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class MapTagResolver implements TagResolver.WithoutArguments, Removable, MappableResolver {
    private Map<String, ? extends Tag> tagMap;

    public MapTagResolver(Map<String, ? extends Tag> tagMap) {
        this.tagMap = tagMap;
    }

    public Map<String, ? extends Tag> tagMap() {
        return tagMap;
    }

    public void tagMap(Map<String, ? extends Tag> tagMap) {
        this.tagMap = tagMap;
    }

    @Override
    public void remove(String tagName) {
        tagMap.remove(tagName);
    }

    @Override
    public @Nullable Tag resolve(final String name) {
        return this.tagMap.get(name);
    }

    @Override
    public boolean has(final String name) {
        return this.tagMap.containsKey(name);
    }

    @Override
    public boolean contributeToMap(final Map<String, Tag> map) {
        map.putAll(this.tagMap);
        return true;
    }

    @Override
    public void contributeKnownNames(final java.util.function.BiConsumer<String, TagResolver> sink) {
        for (final String name : this.tagMap.keySet()) {
            sink.accept(name, this);
        }
    }

    @Override
    public boolean isExhaustivelyKnown() {
        return true;
    }

    @Override
    public boolean mayProducePreProcess() {
        // called once at compile time, no caching needed
        for (final Tag tag : this.tagMap.values()) {
            if (tag instanceof top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.PreProcess) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mayProducePreProcess(final String name) {
        return this.tagMap.get(name) instanceof top.mrxiaom.pluginbase.utils.adventure.sparrow.message.tag.PreProcess;
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MapTagResolver)) {
            return false;
        }
        MapTagResolver that = (MapTagResolver) other;
        return Objects.equals(this.tagMap, that.tagMap);
    }
}
