package top.mrxiaom.pluginbase.utils;

import org.jetbrains.annotations.Nullable;

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

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @SuppressWarnings({"unchecked"})
    public <S, T> Pair<S, T> cast() {
        return (Pair<S, T>) this;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public static String replace(String s, Pair<String, Object>[] replacements) {
        for (Pair<String, Object> replacement : replacements) {
            if (replacement.key.startsWith("__internal__")) continue;
            s = s.replace(replacement.key, String.valueOf(replacement.value));
        }
        return s;
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
