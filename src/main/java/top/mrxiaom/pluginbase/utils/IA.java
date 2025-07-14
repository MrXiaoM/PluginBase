package top.mrxiaom.pluginbase.utils;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class IA {
    private static final Map<String, CustomStack> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static Optional<ItemStack> get(@Nullable String id) {
        if (id == null) return Optional.empty();
        if (cache.containsKey(id)) return Optional.ofNullable(cache.get(id).getItemStack()).map(ItemStack::clone);
        CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) return Optional.empty();
        cache.put(id, stack);
        return Optional.ofNullable(stack.getItemStack()).map(ItemStack::clone);
    }

    public static Optional<String> get(@Nullable ItemStack item) {
        if (item == null) return Optional.empty();
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null) return Optional.empty();
        return Optional.of(stack.getId());
    }

    public static Optional<String> getNamespace(@Nullable ItemStack item) {
        if (item == null) return Optional.empty();
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null) return Optional.empty();
        return Optional.of(stack.getNamespace());
    }

    public static Optional<String> getFullId(@Nullable ItemStack item) {
        if (item == null) return Optional.empty();
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null) return Optional.empty();
        return Optional.of(stack.getNamespace() + ":" + stack.getId());
    }

    public static boolean isMatchFullId(@Nullable ItemStack item, @NotNull String fullId) {
        return fullId.equalsIgnoreCase(getFullId(item).orElse(null));
    }
}
