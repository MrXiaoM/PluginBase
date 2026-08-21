package top.mrxiaom.pluginbase.utils.adventure.serializer.legacy;

import net.kyori.adventure.text.format.TextFormat;

import java.util.Objects;
import java.util.function.Supplier;

public final class WrappedTextFormat implements Supplier<TextFormat> {
    public static final WrappedTextFormat RESET = new WrappedTextFormat(null);
    private final TextFormat format;

    public WrappedTextFormat(TextFormat format) {
        this.format = format;
    }

    @Override
    public TextFormat get() {
        return format;
    }

    public boolean isReset() {
        return format == null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WrappedTextFormat)) return false;
        WrappedTextFormat that = (WrappedTextFormat) o;
        return Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(format);
    }
}
