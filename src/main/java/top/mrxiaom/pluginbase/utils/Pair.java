package top.mrxiaom.pluginbase.utils;

import java.time.LocalDateTime;

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
            s = s.replace(replacement.key, String.valueOf(replacement.value));
        }
        return s;
    }
}
