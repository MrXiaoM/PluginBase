package top.mrxiaom.pluginbase.data;

import java.util.function.Function;
import java.util.function.Supplier;

public class Result<T> {
    private final T value;
    private final Throwable throwable;
    private Result(T value, Throwable throwable) {
        this.value = value;
        this.throwable = throwable;
    }

    public boolean isOk() {
        return throwable == null;
    }

    public T get() {
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                String message = throwable.getClass().getName() + ": " + throwable.getMessage();
                throw new RuntimeException(message, throwable);
            }
        }
        return value;
    }

    public T getOrNull() {
        return value;
    }

    public T orElse(T def) {
        if (throwable != null) {
            return def;
        }
        return value;
    }

    public T orElseGet(Function<Throwable, T> func) {
        if (throwable != null) {
            return func.apply(throwable);
        }
        return value;
    }

    public T orElseGet(Supplier<T> supplier) {
        if (throwable != null) {
            return supplier.get();
        }
        return value;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> fail(Throwable throwable) {
        return new Result<>(null, throwable);
    }

    public static <T> Result<T> illegalArgs(String message) {
        return new Result<>(null, new IllegalArgumentException(message));
    }

    public static <T> Result<T> illegalArgs(String message, Throwable cause) {
        return new Result<>(null, new IllegalArgumentException(message, cause));
    }

    public static <T> Result<T> illegalState(String message) {
        return new Result<>(null, new IllegalStateException(message));
    }

    public static <T> Result<T> illegalState(String message, Throwable cause) {
        return new Result<>(null, new IllegalStateException(message, cause));
    }
}
