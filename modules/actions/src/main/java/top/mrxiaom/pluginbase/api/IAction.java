package top.mrxiaom.pluginbase.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

/**
 * 用户操作实例
 */
public interface IAction {
    /**
     * 该操作的延时执行时间 (tick)
     */
    default long delayAfterRun() {
        return 0L;
    }

    /**
     * @see IAction#run(Player, List)
     */
    default void run(@Nullable Player player) {
        run(player, null);
    }

    /**
     * 立即执行操作
     * @param player 要执行此操作的玩家
     * @param replacements 此操作所需的替换变量
     */
    void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements);
}
