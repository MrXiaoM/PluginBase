package top.mrxiaom.pluginbase.utils.arguments;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CommandArguments {
    private final Arguments arguments;
    private int pointer = 0;
    protected CommandArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public Arguments arguments() {
        return arguments;
    }

    public int pointer() {
        return pointer;
    }

    public void pointer(int pointer) {
        this.pointer = pointer;
    }

    public String last() {
        return arguments.getArgument(pointer - 1);
    }

    public boolean match(String... array) {
        return match(array, null);
    }

    public boolean match(String[] array, @Nullable Supplier<Boolean> extraCondition) {
        String argument = arguments.getArgument(pointer);
        if (argument == null) return false;
        for (String s : array) {
            if (s.equalsIgnoreCase(argument)) {
                if (extraCondition != null && !extraCondition.get()) {
                    return false;
                }
                pointer++;
                return true;
            }
        }
        return false;
    }

    public boolean match(String s) {
        return match(s, null);
    }

    public boolean match(String s, @Nullable Supplier<Boolean> extraCondition) {
        String argument = arguments.getArgument(pointer);
        if (argument == null) return false;
        if (s.equalsIgnoreCase(argument)) {
            if (extraCondition != null && !extraCondition.get()) {
                return false;
            }
            pointer++;
            return true;
        }
        return false;
    }

    @Nullable
    public String nextString() {
        return nextString(null);
    }

    @Contract("!null->!null")
    public String nextString(@Nullable String def) {
        String value = arguments.getArgument(pointer++);
        return value == null ? def : value;
    }

    @Nullable
    public Integer nextInt() {
        return nextInt(NULL(), NULL());
    }

    public Integer nextInt(Integer def) {
        return nextInt(() -> def, NULL());
    }

    public Integer nextInt(Integer def, Integer parseFailed) {
        return nextInt(() -> def, () -> parseFailed);
    }

    public Integer nextInt(@NotNull Supplier<Integer> def) {
        return nextInt(def, NULL());
    }

    public Integer nextInt(@NotNull Supplier<Integer> def, @NotNull Supplier<Integer> parseFailed) {
        String value = arguments.getArgument(pointer++);
        if (value == null) return def.get();
        return Util.parseInt(value).orElseGet(parseFailed);
    }

    @Nullable
    public Double nextDouble() {
        return nextDouble(NULL(), NULL());
    }

    public Double nextDouble(Double def) {
        return nextDouble(() -> def, NULL());
    }

    public Double nextDouble(Double def, Double parseFailed) {
        return nextDouble(() -> def, () -> parseFailed);
    }

    public Double nextDouble(@NotNull Supplier<Double> def) {
        return nextDouble(def, NULL());
    }

    public Double nextDouble(@NotNull Supplier<Double> def, @NotNull Supplier<Double> parseFailed) {
        String value = arguments.getArgument(pointer++);
        if (value == null) return def.get();
        return Util.parseDouble(value).orElseGet(parseFailed);
    }

    @Nullable
    public Float nextFloat() {
        return nextFloat(NULL(), NULL());
    }

    public Float nextFloat(Float def) {
        return nextFloat(() -> def, NULL());
    }

    public Float nextFloat(Float def, Float parseFailed) {
        return nextFloat(() -> def, () -> parseFailed);
    }

    public Float nextFloat(@NotNull Supplier<Float> def) {
        return nextFloat(def, NULL());
    }

    public Float nextFloat(@NotNull Supplier<Float> def, @NotNull Supplier<Float> parseFailed) {
        String value = arguments.getArgument(pointer++);
        if (value == null) return def.get();
        return Util.parseFloat(value).orElseGet(parseFailed);
    }

    @Nullable
    public Long nextLong() {
        return nextLong(NULL(), NULL());
    }

    public Long nextLong(Long def) {
        return nextLong(() -> def, NULL());
    }

    public Long nextLong(Long def, Long parseFailed) {
        return nextLong(() -> def, () -> parseFailed);
    }

    public Long nextLong(@NotNull Supplier<Long> def) {
        return nextLong(def, NULL());
    }

    public Long nextLong(@NotNull Supplier<Long> def, @NotNull Supplier<Long> parseFailed) {
        String value = arguments.getArgument(pointer++);
        if (value == null) return def.get();
        return Util.parseLong(value).orElseGet(parseFailed);
    }

    @Nullable
    public Player nextPlayer() {
        return nextPlayer(null);
    }

    @Contract("!null->!null")
    public Player nextPlayer(Player def) {
        String value = arguments.getArgument(pointer++);
        return Util.getOnlinePlayer(value).orElse(def);
    }

    public Player nextPlayerOrSelf(CommandSender sender, Runnable selfNotPlayerAction) {
        return nextPlayerOrSelf(sender, selfNotPlayerAction, null, null);
    }

    public Player nextPlayerOrSelf(CommandSender sender, Runnable selfNotPlayerAction, String perm, Runnable selfNoPermission) {
        return nextPlayerOrSelf(sender, selfNotPlayerAction, perm, selfNoPermission, null);
    }

    public Player nextPlayerOrSelf(CommandSender sender, Runnable selfNotPlayerAction, String perm, Runnable selfNoPermission, Runnable noPlayerAction) {
        String value = arguments.getArgument(pointer++);
        if (value == null) {
            if (sender instanceof Player) {
                return (Player) sender;
            }
            selfNotPlayerAction.run();
        }
        if (perm != null && !sender.hasPermission(perm)) {
            selfNoPermission.run();
            return null;
        }
        return Util.getOnlinePlayer(value).orElseGet(() -> {
            noPlayerAction.run();
            return null;
        });
    }

    @Nullable
    public OfflinePlayer nextOffline() {
        return nextOffline(null);
    }

    @Contract("!null->!null")
    public OfflinePlayer nextOffline(OfflinePlayer def) {
        String value = arguments.getArgument(pointer++);
        return Util.getOfflinePlayer(value).orElse(def);
    }

    public OfflinePlayer nextOfflineOrSelf(CommandSender sender, Runnable selfNotPlayerAction) {
        return nextOfflineOrSelf(sender, selfNotPlayerAction, null, null);
    }

    public OfflinePlayer nextOfflineOrSelf(CommandSender sender, Runnable selfNotPlayerAction, String perm, Runnable selfNoPermission) {
        return nextOfflineOrSelf(sender, selfNotPlayerAction, perm, selfNoPermission, null);
    }

    public OfflinePlayer nextOfflineOrSelf(CommandSender sender, Runnable selfNotPlayerAction, String perm, Runnable selfNoPermission, Runnable noPlayerAction) {
        String value = arguments.getArgument(pointer++);
        if (value == null) {
            if (sender instanceof OfflinePlayer) {
                return (OfflinePlayer) sender;
            }
            selfNotPlayerAction.run();
        }
        if (perm != null && !sender.hasPermission(perm)) {
            selfNoPermission.run();
            return null;
        }
        return Util.getOfflinePlayer(value).orElseGet(() -> {
            noPlayerAction.run();
            return null;
        });
    }

    @Nullable
    public <T> T nextValueOf(Class<T> type) {
        return nextValueOf(type, null);
    }

    @Contract("_,!null->!null")
    public <T> T nextValueOf(Class<T> type, T def) {
        String value = arguments.getArgument(pointer++);
        return Util.valueOr(type, value, def);
    }

    @Nullable
    public <T> T next(Function<String, T> transformer) {
        return next(transformer, null);
    }

    public <T> T next(Function<String, T> transformer, T def) {
        String value = arguments.getArgument(pointer++);
        if (value == null) return def;
        T apply = transformer.apply(value);
        return apply == null ? def : apply;
    }

    public <T> T nextOptional(Function<String, T> transformer) {
        String value = arguments.getArgument(pointer++);
        return transformer.apply(value);
    }

    public <T> T to(Function<String[], T> transformer) {
        T another = arguments.to(transformer);
        if (another instanceof CommandArguments) {
            ((CommandArguments) another).pointer(pointer);
        }
        return another;
    }

    public static <T> Supplier<T> NULL() {
        return () -> null;
    }

    public static CommandArguments simple(String[] args) {
        return SimpleCommandArguments.of(args);
    }
}
