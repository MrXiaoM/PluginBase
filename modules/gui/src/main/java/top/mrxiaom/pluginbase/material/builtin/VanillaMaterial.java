package top.mrxiaom.pluginbase.material.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.material.IMaterial;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

public class VanillaMaterial implements IMaterial.Provider {
    public static final IMaterial.Provider PROVIDER = new VanillaMaterial();
    public static final IMaterial DEFAULT = new Impl(Material.PAPER, null);
    private VanillaMaterial() {}

    @Override
    public @Nullable IMaterial parse(@NotNull String input) {
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(input);
        if (pair != null) {
            return new Impl(pair.key(), pair.value());
        }
        return null;
    }

    public static class Impl implements IMaterial {
        private final @NotNull Material material;
        private final @Nullable Integer dataValue;
        public Impl(@NotNull Material material, @Nullable Integer dataValue) {
            this.material = material;
            this.dataValue = dataValue;
        }

        @Override
        public @NotNull ItemStack create(@Nullable Player player, int amount) {
            if (dataValue != null) {
                // noinspection deprecation
                return new ItemStack(material, amount, dataValue.shortValue());
            } else {
                return new ItemStack(material, amount);
            }
        }
    }
}
