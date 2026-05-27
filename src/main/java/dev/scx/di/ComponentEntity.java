package dev.scx.di;

import dev.scx.di.assembly.ComponentAssemblyContext;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

/// ComponentEntity
///
/// @author scx567888
/// @version 0.0.1
sealed interface ComponentEntity permits PrototypeComponentEntity, SingletonComponentEntity {

    /// 获取组件
    Object getComponent(ComponentAssemblyContext context) throws ComponentCreationException;

    /// 获取 Component 的类型
    TypeInfo componentType();

    /// 是否单例
    boolean isSingleton();

}
