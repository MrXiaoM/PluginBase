package top.mrxiaom.pluginbase.utils;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class IA {
    private static final Map<String, CustomStack> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static Optional<ItemStack> get(String id) {
        if (cache.containsKey(id)) return Optional.ofNullable(cache.get(id).getItemStack()).map(ItemStack::clone);
        CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) return Optional.empty();
        cache.put(id, stack);
        return Optional.ofNullable(stack.getItemStack()).map(ItemStack::clone);
    }

    public static Optional<String> get(ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null) return Optional.empty();
        return Optional.of(stack.getId());
    }
}
