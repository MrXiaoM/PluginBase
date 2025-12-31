package top.mrxiaom.pluginbase.utils;

import org.jetbrains.annotations.Nullable;

public abstract class ConditionalReplacer<Context> {

    @Nullable
    public abstract String doReplace(String front, String input, Context context);

    public String replaceOrNull(String input, Context context) {
        if (input.startsWith("$")) {
            String substring = input.substring(1);
            int index = substring.indexOf('$');
            if (index < 0) return null;
            String front = substring.substring(0, index);
            String newInput = substring.substring(index + 1);
            return doReplace(front, newInput, context);
        }
        return null;
    }
}
