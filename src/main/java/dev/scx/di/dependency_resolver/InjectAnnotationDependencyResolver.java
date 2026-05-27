package dev.scx.di.dependency_resolver;

import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_point.ConstructorParameterDependencyPoint;
import dev.scx.di.dependency_point.DependencyPoint;
import dev.scx.di.dependency_point.FieldDependencyPoint;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.di.exception.NoSuchComponentException;
import dev.scx.di.exception.NoUniqueComponentException;
import dev.scx.reflect.FieldInfo;
import dev.scx.reflect.ParameterInfo;

import static dev.scx.di.dependency_resolver.DependencyResolutionIntent.*;

/// 处理 [Inject] 注解 同时也承担最核心的 配置
///
/// @author scx567888
/// @version 0.0.1
public final class InjectAnnotationDependencyResolver implements DependencyResolver {

    public InjectAnnotationDependencyResolver() {

    }

    @Override
    public DependencyResolutionIntent match(DependencyPoint dependencyPoint) {
        return switch (dependencyPoint) {
            case ConstructorParameterDependencyPoint c -> matchConstructorArgument(c.parameter());
            case FieldDependencyPoint f -> matchFieldValue(f.field());
        };
    }

    @Override
    public Object resolve(DependencyPoint dependencyPoint, DependencyResolverContext context) throws Exception {
        return switch (dependencyPoint) {
            case ConstructorParameterDependencyPoint c -> resolveConstructorArgument(c.parameter(), context);
            case FieldDependencyPoint f -> resolveFieldValue(f.field(), context);
        };
    }

    public DependencyResolutionIntent matchConstructorArgument(ParameterInfo parameterInfo) {
        var injectAnnotation = parameterInfo.findAnnotation(Inject.class);
        // 没写注解 我们也可能尝试处理 (这是构造函数阶段的特殊处理)
        if (injectAnnotation == null) {
            return CANDIDATE;
        }
        // 如果写了注解就强制处理
        return REQUIRED;
    }

    public DependencyResolutionIntent matchFieldValue(FieldInfo fieldInfo) {
        var injectAnnotation = fieldInfo.findAnnotation(Inject.class);
        // 没写注解 无法处理
        if (injectAnnotation == null) {
            return NOT_APPLICABLE;
        }
        // 如果写了注解就强制处理
        return REQUIRED;
    }

    public Object resolveConstructorArgument(ParameterInfo parameterInfo, DependencyResolverContext context) throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        // 构造参数和 field 注入规则略有不同, 允许没有注解
        var injectAnnotation = parameterInfo.findAnnotation(Inject.class);
        String name = null;
        if (injectAnnotation != null && injectAnnotation.value().length > 0) {
            name = injectAnnotation.value()[0];
        }
        if (name != null) {
            return context.getComponent(name, parameterInfo.parameterType());
        } else {
            return context.getComponent(parameterInfo.parameterType());
        }
    }

    public Object resolveFieldValue(FieldInfo fieldInfo, DependencyResolverContext context) throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        // 字段 只处理有 Inject 注解的
        var injectAnnotation = fieldInfo.findAnnotation(Inject.class);
        // 这里理论不可能是 null 此处防御处理
        if (injectAnnotation == null) {
            throw new IllegalStateException("Inject is null");
        }
        String name = null;
        if (injectAnnotation.value().length > 0) {
            name = injectAnnotation.value()[0];
        }
        if (name != null) {
            return context.getComponent(name, fieldInfo.fieldType());
        } else {
            return context.getComponent(fieldInfo.fieldType());
        }
    }

}
