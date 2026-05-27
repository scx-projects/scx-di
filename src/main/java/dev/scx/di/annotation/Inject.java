package dev.scx.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 依赖注入注解:
///
/// - 1. 字段注入 -> @Inject 或 @Inject("componentName")
/// - 2. 参数注入 -> @Inject 或 @Inject("componentName")
/// - 3. 构造函数注入 -> @Inject (标记该构造函数可用于 DI)
///
/// > 注意: 构造函数注入时, value 参数被忽略.
///
/// @author scx567888
/// @version 0.0.1
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    String[] value() default {};

}
