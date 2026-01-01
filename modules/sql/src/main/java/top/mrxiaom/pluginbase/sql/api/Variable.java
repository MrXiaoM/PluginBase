package top.mrxiaom.pluginbase.sql.api;

/**
 * 方法调用变量
 */
public interface Variable {
    /**
     * 根据方法调用上下文，反射获取变量的值
     */
    Object get(MethodContext ctx) throws Throwable;
}
