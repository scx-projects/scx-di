package dev.scx.di.dependency_resolver;

import dev.scx.di.annotation.Value;
import dev.scx.di.dependency_point.ConstructorParameterDependencyPoint;
import dev.scx.di.dependency_point.DependencyPoint;
import dev.scx.di.dependency_point.FieldDependencyPoint;
import dev.scx.reflect.AnnotatedElementInfo;
import dev.scx.reflect.TypeInfo;

import static dev.scx.di.dependency_resolver.DependencyResolutionIntent.NOT_APPLICABLE;
import static dev.scx.di.dependency_resolver.DependencyResolutionIntent.REQUIRED;

/// 处理 [Value] 注解
///
/// @author scx567888
public final class ValueAnnotationDependencyResolver implements DependencyResolver {

    private final ValueResolver valueResolver;

    public ValueAnnotationDependencyResolver(ValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    public DependencyResolutionIntent matchValue(AnnotatedElementInfo annotatedElement) {
        var valueAnnotation = annotatedElement.findAnnotation(Value.class);
        // 没注解就不处理
        if (valueAnnotation == null) {
            return NOT_APPLICABLE;
        }
        // 有注解就强制处理
        return REQUIRED;
    }

    public Object resolveValue(AnnotatedElementInfo annotatedElement, TypeInfo targetType) throws Exception {
        var valueAnnotation = annotatedElement.findAnnotation(Value.class);
        // 这里理论不可能是 null 此处防御处理
        if (valueAnnotation == null) {
            throw new IllegalStateException("Value is null");
        }
        return valueResolver.resolveValue(valueAnnotation.value(), targetType);
    }

    @Override
    public DependencyResolutionIntent match(DependencyPoint dependencyPoint) {
        return switch (dependencyPoint) {
            case ConstructorParameterDependencyPoint c -> matchValue(c.parameter());
            case FieldDependencyPoint f -> matchValue(f.field());
        };
    }

    @Override
    public Object resolve(DependencyPoint dependencyPoint, DependencyResolverContext context) throws Exception {
        return switch (dependencyPoint) {
            case ConstructorParameterDependencyPoint c -> resolveValue(c.parameter(), c.parameter().parameterType());
            case FieldDependencyPoint f -> resolveValue(f.field(), f.field().fieldType());
        };
    }

    /// 负责根据 [Value] 中声明的 key 解析实际值.
    ///
    /// 具体如何取值、缺失值如何处理、`null` 是否合法、是否进行类型转换,
    /// 都由实现类自行决定.
    ///
    /// 当 key 不存在或值无法解析时, 实现类可以返回 `null`, 也可以选择抛出异常.
    ///
    /// @author scx567888
    public interface ValueResolver {

        /// 根据 key 和目标类型解析值.
        ///
        /// @param key        [Value] 中声明的 key
        /// @param targetType 注入点期望的目标类型
        /// @return 解析后的值; 可以为 `null`, 具体含义由实现类决定
        /// @throws Exception 当实现类选择以异常表示解析失败时抛出
        Object resolveValue(String key, TypeInfo targetType) throws Exception;

    }

}
