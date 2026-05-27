package dev.scx.di.assembly;

import dev.scx.di.exception.ComponentCreationException;

/// ComponentInitializer 是组件初始化阶段的内部抽象.
///
/// @author scx567888
/// @version 0.0.1
/// @see ComponentCreator
public sealed interface ComponentInitializer permits FieldInjectComponentInitializer {

    /// 初始化一个已经存在的 Component 实例.
    ///
    /// 实现类可以通过 context.resolveDependency(...) 解析初始化阶段所需的依赖,
    /// 但不应该绕过 context 直接访问容器内部结构.
    ///
    /// @param component 已经创建出来、但尚未完成初始化的 Component 实例
    /// @param context   当前组件解析上下文
    /// @throws ComponentCreationException 初始化 Component 失败时抛出
    void initializeComponent(Object component, ComponentAssemblyContext context) throws ComponentCreationException;

}
