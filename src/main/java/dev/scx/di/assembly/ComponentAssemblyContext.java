package dev.scx.di.assembly;

import dev.scx.di.dependency_point.DependencyPoint;

/// ComponentAssemblyContext
///
/// @author scx567888
/// @version 0.0.1
public interface ComponentAssemblyContext {

    /// 解析构造函数参数
    Object resolveDependency(DependencyPoint dependencyPoint) throws Exception;

}
