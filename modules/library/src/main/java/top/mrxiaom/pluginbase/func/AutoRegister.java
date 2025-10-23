package top.mrxiaom.pluginbase.func;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动注册标记，插件启用时会扫描所有类，给类添加这个注解，将会自动加载这个模块
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegister {
    /**
     * 这个模块需要依赖的插件，若服务器未安装指定插件，该模块不会加载
     */
    String[] requirePlugins() default {};

    /**
     * 模块加载优先级，数值越小越先加载
     */
    int priority() default 1000;
}
