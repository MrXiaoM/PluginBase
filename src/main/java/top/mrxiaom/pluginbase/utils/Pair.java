package top.mrxiaom.pluginbase.utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Pair<K, V> {
    K key;
    V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public K key() {
        return key;
    }

    public K left() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void key(K key) {
        this.key = key;
    }

    public void left(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public V value() {
        return value;
    }

    public V right() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void value(V value) {
        this.value = value;
    }

    public void right(V value) {
        this.value = value;
    }

    @SuppressWarnings({"unchecked"})
    public <S, T> Pair<S, T> cast() {
        return (Pair<S, T>) this;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public static List<String> replace(List<String> list, @Nullable Iterable<Pair<String, Object>> replacements) {
        if (replacements == null) return new ArrayList<>(list);
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(replace(s, replacements));
        }
        return result;
    }

    public static List<String> replace(List<String> list, Pair<String, Object> @Nullable [] replacements) {
        if (replacements == null) return new ArrayList<>(list);
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(replace(s, replacements));
        }
        return result;
    }

    @SafeVarargs
    public static List<String> replace0(List<String> list, Pair<String, Object>... replacements) {
        return replace(list, replacements);
    }

    public static String replace(String s, @Nullable Iterable<Pair<String, Object>> replacements) {
        if (replacements == null) return s;
        for (Pair<String, Object> replacement : replacements) {
            if (replacement.key.startsWith("__internal__")) continue;
            s = s.replace(replacement.key, String.valueOf(replacement.value));
        }
        return s;
    }

    public static String replace(String s, Pair<String, Object> @Nullable [] replacements) {
        if (replacements == null) return s;
        for (Pair<String, Object> replacement : replacements) {
            if (replacement.key.startsWith("__internal__")) continue;
            s = s.replace(replacement.key, String.valueOf(replacement.value));
        }
        return s;
    }

    @SafeVarargs
    public static String replace0(String s, Pair<String, Object>... replacements) {
        return replace(s, replacements);
    }

    @Nullable
    public static <K, V> V firstOrNull(Pair<K, V>[] array, K key) {
        for (Pair<K, V> pair : array) {
            if (key == null) {
                if (pair.key == null) {
                    return pair.value;
                }
            } else if (key.equals(pair.key)) {
                return pair.value;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static <K, V> Pair<K, V>[] array(int length) {
        return new Pair[length];
    }
}
