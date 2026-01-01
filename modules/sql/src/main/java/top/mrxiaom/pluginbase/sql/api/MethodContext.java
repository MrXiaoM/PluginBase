package top.mrxiaom.pluginbase.sql.api;

import java.util.Map;

public class MethodContext {
    private final String methodName;
    private final Object[] args;
    private final Map<String, String> plainVariables;
    public MethodContext(String methodName, Object[] args, Map<String, String> plainVariables) {
        this.methodName = methodName;
        this.args = args;
        this.plainVariables = plainVariables;
    }

    public String getMethodName() {
        return methodName;
    }

    public Map<String, String> getPlainVariables() {
        return plainVariables;
    }

    public String replacePlainVariables(String str) {
        String s = str;
        for (Map.Entry<String, String> entry : plainVariables.entrySet()) {
            s = s.replace("#{" + entry.getKey() + "}", entry.getValue());
        }
        return s;
    }

    public Object getArgument(int index) {
        if (index < 0 || index >= args.length) return null;
        return args[index];
    }
}
