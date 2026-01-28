package top.mrxiaom.pluginbase.configuration;

import org.jetbrains.annotations.NotNull;

/**
 * Various settings for controlling the input and output of a {@link
 * top.mrxiaom.pluginbase.configuration.MemoryConfiguration}
 */
public class MemoryConfigurationOptions extends ConfigurationOptions {
    protected MemoryConfigurationOptions(@NotNull top.mrxiaom.pluginbase.configuration.MemoryConfiguration configuration) {
        super(configuration);
    }

    @NotNull
    @Override
    public top.mrxiaom.pluginbase.configuration.MemoryConfiguration configuration() {
        return (MemoryConfiguration) super.configuration();
    }

    @NotNull
    @Override
    public MemoryConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @NotNull
    @Override
    public MemoryConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }
}
