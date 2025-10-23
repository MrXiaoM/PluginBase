package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionUtils {
    public static List<String> split(String string, String spliter) {
        return split(string, spliter, 0);
    }

    public static List<String> split(String string, String spliter, int limit) {
        int oldIndex = 0;
        int length = spliter.length();
        if (length == 0) throw new IllegalArgumentException("spliter can't be empty!");
        int count = limit - 1;
        boolean unlimited = limit <= 0;
        List<String> list = new ArrayList<>();
        while (unlimited || list.size() < count) {
            int i = string.indexOf(spliter, oldIndex);
            if (i >= 0) {
                if (oldIndex == i) {
                    list.add("");
                } else {
                    list.add(string.substring(oldIndex, i));
                }
                oldIndex = i + length;
            } else {
                break;
            }
        }
        list.add(string.substring(oldIndex));
        return list;
    }

    public static List<String> split(String string, char spliter) {
        return split(string, spliter, 0);
    }

    public static List<String> split(String string, char spliter, int limit) {
        int oldIndex = 0;
        int count = limit - 1;
        boolean unlimited = limit <= 0;
        List<String> list = new ArrayList<>();
        while (unlimited || list.size() < count) {
            int i = string.indexOf(spliter, oldIndex);
            if (i >= 0) {
                if (oldIndex == i) {
                    list.add("");
                } else {
                    list.add(string.substring(oldIndex, i));
                }
                oldIndex = i + 1;
            } else {
                break;
            }
        }
        list.add(string.substring(oldIndex));
        return list;
    }

    public static List<String> startsWith(String s, String... texts) {
        return startsWith(s, Lists.newArrayList(texts));
    }

    public static List<String> startsWith(String s, Iterable<String> texts) {
        List<String> list = new ArrayList<>();
        s = s.toLowerCase();
        for (String text : texts) {
            if (text.toLowerCase().startsWith(s)) list.add(text);
        }
        return list;
    }

    /**
     * 从 Map 中读取，如果不存在，则创建、加入到 Map，并返回
     */
    @NotNull
    public static <K, V> V getOrPut(Map<K, V> map, K key, Function<K, V> creator) {
        V value = map.get(key);
        if (value != null) return value;
        V newValue = creator.apply(key);
        map.put(key, newValue);
        return newValue;
    }

    /**
     * 从 Map 中读取，如果不存在，则创建、加入到 Map，并返回
     */
    @NotNull
    public static <K, V> V getOrPut(Map<K, V> map, K key, Supplier<V> creator) {
        V value = map.get(key);
        if (value != null) return value;
        V newValue = creator.get();
        map.put(key, newValue);
        return newValue;
    }

    /**
     * 将列表分割按每多少个一组，分割为多份，其中最后一份的数量可能数量不足<br>
     * 与 kotlin 的 <code>List.chunk(Int)</code> 基本相同
     * @param list 列表
     * @param size 每份多少个元素
     */
    public static <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        List<T> temp = new ArrayList<>();
        for (T item : list) {
            temp.add(item);
            if (temp.size() == size) {
                result.add(temp);
                temp = new ArrayList<>();
            }
        }
        if (!temp.isEmpty()) result.add(temp);
        return result;
    }

    /**
     * 按正则表达式的 group 分割字符串
     * @param regex 正则表达式
     * @param s 字符串
     * @param consumer 分割出来的每一份处理逻辑
     */
    public static void split(Pattern regex, String s, Consumer<Util.RegexResult> consumer) {
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                consumer.accept(new Util.RegexResult(null, s.substring(index, first)));
            }
            consumer.accept(new Util.RegexResult(m.toMatchResult(), s.substring(first, last)));
            index = last;
        }
        if (index < s.length()) {
            consumer.accept(new Util.RegexResult(null, s.substring(index)));
        }
    }

    /**
     * 按正则表达式的 group 分割字符串，并添加到列表
     * @param regex 正则表达式
     * @param s 字符串
     * @param transform 分割出来的每一份处理逻辑
     */
    public static <T> List<T> split(Pattern regex, String s, Function<Util.RegexResult, T> transform) {
        List<T> list = new ArrayList<>();
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                T value = transform.apply(new Util.RegexResult(null, s.substring(index, first)));
                if (value != null) list.add(value);
            }
            T value = transform.apply(new Util.RegexResult(m.toMatchResult(), s.substring(first, last)));
            if (value != null) list.add(value);
            index = last;
        }
        if (index < s.length()) {
            T value = transform.apply(new Util.RegexResult(null, s.substring(index)));
            if (value != null) list.add(value);
        }
        return list;
    }
}
