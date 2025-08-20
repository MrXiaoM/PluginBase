package top.mrxiaom.pluginbase.utils.arguments;

public class SimpleCommandArguments extends CommandArguments {
    private static final Arguments.Builder builder = Arguments.builder();
    protected SimpleCommandArguments(Arguments arguments) {
        super(arguments);
    }

    public static SimpleCommandArguments of(String[] args) {
        return builder.build(SimpleCommandArguments::new, args);
    }
}
