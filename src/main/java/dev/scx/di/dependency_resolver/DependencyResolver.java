package dev.scx.di.dependency_resolver;

import dev.scx.di.dependency_point.DependencyPoint;

/// 提供配置一个 Component 所需的依赖
///
/// @author scx567888
public interface DependencyResolver {

    /// 匹配 依赖点.
    DependencyResolutionIntent match(DependencyPoint dependencyPoint);

    /// 解析 依赖点.
    Object resolve(DependencyPoint dependencyPoint, DependencyResolverContext context) throws Exception;

}
