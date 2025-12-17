package top.mrxiaom.pluginbase.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

/**
 * <code>List&lt;Pair&lt;K, V&gt;&gt;</code> 的包装类型，其中添加了一些实用的方法。<br>
 * 默认实现为 <code>ArrayList</code>。
 */
public class ListPair<K, V> implements List<Pair<K, V>> {
    private final List<Pair<K, V>> proxy;
    public ListPair(List<Pair<K, V>> proxy) {
        this.proxy = proxy;
    }
    public ListPair() {
        this(new ArrayList<>());
    }

    @Override
    public int size() {
        return proxy.size();
    }

    @Override
    public boolean isEmpty() {
        return proxy.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return proxy.contains(o);
    }

    @NotNull
    @Override
    public Iterator<Pair<K, V>> iterator() {
        return proxy.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return proxy.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return proxy.toArray(a);
    }

    public Pair<K, V>[] toPairArray() {
        Pair<K, V>[] pairs = Pair.array(size());
        return proxy.toArray(pairs);
    }

    @Override
    public boolean add(Pair<K, V> kvPair) {
        return proxy.add(kvPair);
    }

    public boolean add(K key, V value) {
        return add(Pair.of(key, value));
    }

    /**
     * 该方法仅限在泛型 V 的类型为 Object 时使用
     */
    @SuppressWarnings({"unchecked"})
    public boolean add(K key, Supplier<V> supplier) {
        return add(Pair.of(key, (V) supplier));
    }

    @Override
    public boolean remove(Object o) {
        return proxy.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(proxy).containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Pair<K, V>> c) {
        return proxy.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends Pair<K, V>> c) {
        return proxy.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return proxy.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return proxy.retainAll(c);
    }

    @Override
    public void clear() {
        proxy.clear();
    }

    @Override
    public Pair<K, V> get(int index) {
        return proxy.get(index);
    }

    @Override
    public Pair<K, V> set(int index, Pair<K, V> element) {
        return proxy.set(index, element);
    }

    public Pair<K, V> set(int index, K key, V value) {
        return set(index, Pair.of(key, value));
    }

    @Override
    public void add(int index, Pair<K, V> element) {
        proxy.add(index, element);
    }

    public void add(int index, K key, V value) {
        add(index, Pair.of(key, value));
    }

    @Override
    public Pair<K, V> remove(int index) {
        return proxy.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return proxy.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return proxy.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<Pair<K, V>> listIterator() {
        return proxy.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<Pair<K, V>> listIterator(int index) {
        return proxy.listIterator(index);
    }

    @NotNull
    @Override
    public List<Pair<K, V>> subList(int fromIndex, int toIndex) {
        return proxy.subList(fromIndex, toIndex);
    }
}
