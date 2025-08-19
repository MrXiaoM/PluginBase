package top.mrxiaom.pluginbase.func.language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可选注解，用于设置语言键前缀
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Language {
    /**
     * 语言键前缀，仅用于任意类、枚举类
     */
    String prefix() default "";

    /**
     * 语言键名，仅用于字段
     */
    String value() default "";
}
