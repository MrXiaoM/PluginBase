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

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public static void main(String[] args) {
        timeFromString("[2024-06-08 18:36:36.520]");
    }

    private static LocalDateTime timeFromString(String s) {
        String year = s.substring(1, 5);
        String month = s.substring(6, 8);
        String date = s.substring(9, 11);
        String hour = s.substring(12, 14);
        String minute = s.substring(15, 17);
        String second = s.substring(18, 20);
        String mills = s.substring(21, 24);
        System.out.println(year + month + date + hour + minute + second + mills);
        return null;
    }
}
