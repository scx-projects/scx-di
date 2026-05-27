package dev.scx.di.dependency_resolver;

import dev.scx.di.exception.ComponentCreationException;
import dev.scx.di.exception.NoSuchComponentException;
import dev.scx.di.exception.NoUniqueComponentException;
import dev.scx.reflect.TypeInfo;

/// DependencyResolverContext
///
/// @author scx567888
/// @version 0.0.1
public interface DependencyResolverContext {

    /// 根据 名称 获取 Component
    Object getComponent(String name) throws NoSuchComponentException, ComponentCreationException;

    /// 根据 类型 获取 Component
    <T> T getComponent(TypeInfo type) throws NoSuchComponentException, NoUniqueComponentException, ComponentCreationException;

    /// 根据 名称和类型 获取 Component
    <T> T getComponent(String name, TypeInfo type) throws NoSuchComponentException, ComponentCreationException;

}
