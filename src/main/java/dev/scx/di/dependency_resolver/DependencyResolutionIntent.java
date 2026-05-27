package dev.scx.di.dependency_resolver;

/// 依赖解析意图
///
/// @author scx567888
/// @version 0.0.1
public enum DependencyResolutionIntent {

    /// 不能处理
    NOT_APPLICABLE,

    /// 可以处理
    CANDIDATE,

    /// 必须处理
    REQUIRED

}
