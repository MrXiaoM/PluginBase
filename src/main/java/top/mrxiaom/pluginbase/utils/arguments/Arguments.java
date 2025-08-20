package top.mrxiaom.pluginbase.utils.arguments;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Arguments {
    private final Map<String, Boolean> booleanMap;
    private final Map<String, String> stringMap;
    private final List<String> args;
    private final String[] originalArgs;
    private Arguments(String[] originalArgs, List<String> args, Map<String, Boolean> booleanMap, Map<String, String> stringMap) {
        this.originalArgs = originalArgs;
        this.args = args;
        this.booleanMap = booleanMap;
        this.stringMap = stringMap;
    }

    public boolean getOptionBoolean(String key) {
        return booleanMap.getOrDefault(key, false);
    }

    public String getOptionString(String key, String def) {
        return stringMap.getOrDefault(key, def);
    }

    @Nullable
    public String getArgument(int index) {
        if (index >= args.size()) {
            return null;
        } else {
            return args.get(index);
        }
    }

    public String[] getOriginalArgs() {
        return originalArgs;
    }

    public <T> T to(Function<String[], T> transformer) {
        return transformer.apply(originalArgs);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> booleanOptions = new HashMap<>();
        private final Map<String, String> stringOptions = new HashMap<>();
        private Builder() {}

        public Builder addBooleanOption(String key, String... options) {
            for (String option : options) {
                booleanOptions.put(option, key);
            }
            return this;
        }

        public Builder addStringOptions(String key, String... options) {
            for (String option : options) {
                stringOptions.put(option + "=", key);
            }
            return this;
        }

        private Pair<String, String> getStringOption(String input) {
            for (Map.Entry<String, String> entry : stringOptions.entrySet()) {
                if (input.startsWith(entry.getKey())) {
                    return Pair.of(entry.getKey(), entry.getValue());
                }
            }
            return null;
        }

        public <T> T build(Function<Arguments, T> func, String[] args) {
            List<String> arguments = new ArrayList<>();
            Map<String, Boolean> booleanMap = new HashMap<>();
            Map<String, String> stringMap = new HashMap<>();
            boolean options = false;

            String keyString = null;
            String keyStringEndQuote = null;
            StringBuilder keyStringBuilder = null;

            for (String arg : args) {
                if (keyString != null) {
                    keyStringBuilder.append(" ");
                    if (arg.endsWith(keyStringEndQuote)) {
                        keyStringBuilder.append(arg, 0, arg.length() - 1);
                        keyString = null;
                        keyStringEndQuote = null;
                        keyStringBuilder = null;
                    } else {
                        keyStringBuilder.append(arg);
                    }
                    continue;
                }
                String keyBoolean = booleanOptions.get(arg);
                if (keyBoolean != null) {
                    options = true;
                    booleanMap.put(keyBoolean, true);
                    continue;
                }
                Pair<String, String> stringOptionPair = getStringOption(arg);
                if (stringOptionPair != null) {
                    options = true;
                    String key = stringOptionPair.value();
                    String str = arg.substring(stringOptionPair.key().length());
                    if (str.startsWith("'")) {
                        if (str.endsWith("'")) {
                            stringMap.put(key, str.substring(1, str.length() - 1));
                            continue;
                        }
                        keyString = key;
                        keyStringEndQuote = "'";
                        keyStringBuilder = new StringBuilder(str.substring(1));
                        continue;
                    }
                    if (str.startsWith("\"")) {
                        if (str.endsWith("\"")) {
                            stringMap.put(key, str.substring(1, str.length() - 1));
                            continue;
                        }
                        keyString = key;
                        keyStringEndQuote = "\"";
                        keyStringBuilder = new StringBuilder(str.substring(1));
                        continue;
                    }
                    if (str.startsWith("`")) {
                        if (str.endsWith("`")) {
                            stringMap.put(key, str.substring(1, str.length() - 1));
                            continue;
                        }
                        keyString = key;
                        keyStringEndQuote = "`";
                        keyStringBuilder = new StringBuilder(str.substring(1));
                        continue;
                    }
                    stringMap.put(key, str);
                    continue;
                }
                if (!options) {
                    arguments.add(arg);
                }
            }
            return func.apply(new Arguments(args, arguments, booleanMap, stringMap));
        }
    }
}
