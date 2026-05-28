package top.mrxiaom.pluginbase.material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;

public interface IMaterial {

    @NotNull ItemStack create(@Nullable Player player, int amount);

    interface Provider extends WithPriority {
        @Nullable IMaterial parse(@NotNull String input);
    }
}
