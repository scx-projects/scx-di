package dev.scx.di;

import dev.scx.di.dependency_resolver.DependencyResolver;
import dev.scx.di.exception.DuplicateComponentNameException;
import dev.scx.di.exception.IllegalComponentTypeException;
import dev.scx.di.exception.NoSuchConstructorException;
import dev.scx.di.exception.NoUniqueConstructorException;
import dev.scx.reflect.TypeInfo;
import dev.scx.reflect.TypeReference;

import static dev.scx.reflect.ScxReflect.typeOf;

/// ComponentContainerBuilder
///
/// @author scx567888
public interface ComponentContainerBuilder {

    /// 根据 TypeInfo 注册一个 Component
    ///
    /// @param isSingleton 是否单例
    /// @param injectField 是否注入字段
    ComponentContainerBuilder registerComponentType(String name, TypeInfo componentType, boolean isSingleton, boolean injectField) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException;

    /// 注册一个单例的 Component
    ///
    /// @param injectField 是否注入字段
    ComponentContainerBuilder registerComponent(String name, Object componentInstance, boolean injectField) throws DuplicateComponentNameException;

    /// 添加依赖解析器
    ComponentContainerBuilder addDependencyResolver(DependencyResolver dependencyResolver);

    /// build
    ComponentContainer build();

    /// 根据 TypeInfo 注册一个 Component (默认 注入字段)
    ///
    /// @param isSingleton 是否单例
    default ComponentContainerBuilder registerComponentType(String name, TypeInfo componentType, boolean isSingleton) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, componentType, isSingleton, true);
    }

    /// 根据 TypeInfo 注册一个 Component (默认 注入字段, 单例模式)
    default ComponentContainerBuilder registerComponentType(String name, TypeInfo componentType) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, componentType, true, true);
    }

    /// 根据 Class 注册一个 Component
    ///
    /// @param isSingleton 是否单例
    /// @param injectField 是否注入字段
    default ComponentContainerBuilder registerComponentType(String name, Class<?> componentType, boolean isSingleton, boolean injectField) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, typeOf(componentType), isSingleton, injectField);
    }

    /// 根据 Class 注册一个 Component (默认 注入字段)
    ///
    /// @param isSingleton 是否单例
    default ComponentContainerBuilder registerComponentType(String name, Class<?> componentType, boolean isSingleton) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, componentType, isSingleton, true);
    }

    /// 根据 Class 注册一个 Component (默认 注入字段, 单例模式)
    default ComponentContainerBuilder registerComponentType(String name, Class<?> componentType) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, componentType, true, true);
    }

    /// 根据 TypeReference 注册一个 Component
    ///
    /// @param isSingleton 是否单例
    /// @param injectField 是否注入字段
    default ComponentContainerBuilder registerComponentType(String name, TypeReference<?> componentType, boolean isSingleton, boolean injectField) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, typeOf(componentType), isSingleton, injectField);
    }

    /// 根据 TypeReference 注册一个 Component (默认 注入字段)
    ///
    /// @param isSingleton 是否单例
    default ComponentContainerBuilder registerComponentType(String name, TypeReference<?> componentType, boolean isSingleton) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, componentType, isSingleton, true);
    }

    /// 根据 TypeReference 注册一个 Component (默认 注入字段, 单例模式)
    default ComponentContainerBuilder registerComponentType(String name, TypeReference<?> componentType) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        return registerComponentType(name, componentType, true, true);
    }

    /// 注册一个已有实例 Component (默认 不注入字段)
    default ComponentContainerBuilder registerComponent(String name, Object component) throws DuplicateComponentNameException {
        return registerComponent(name, component, false);
    }

}
