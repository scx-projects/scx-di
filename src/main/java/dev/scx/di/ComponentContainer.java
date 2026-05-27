package dev.scx.di;

import dev.scx.di.exception.ComponentCreationException;
import dev.scx.di.exception.NoSuchComponentException;
import dev.scx.di.exception.NoUniqueComponentException;
import dev.scx.reflect.TypeInfo;
import dev.scx.reflect.TypeReference;

import java.util.Map;

import static dev.scx.reflect.ScxReflect.typeOf;

/// ComponentContainer
///
/// @author scx567888
/// @version 0.0.1
public interface ComponentContainer {

    static ComponentContainerBuilder builder() {
        return new DefaultComponentContainerBuilder();
    }

    /// 获取所有 Component 定义 (只读)
    Map<String, ComponentDefinition> componentDefinitions();

    /// 根据 名称 获取 Component
    Object getComponent(String name) throws NoSuchComponentException, ComponentCreationException;

    /// 根据 类型 获取 Component
    <T> T getComponent(TypeInfo type) throws NoSuchComponentException, NoUniqueComponentException, ComponentCreationException;

    /// 根据 名称和类型 获取 Component
    <T> T getComponent(String name, TypeInfo type) throws NoSuchComponentException, ComponentCreationException;

    /// 根据 类型 获取 Component
    default <T> T getComponent(Class<T> type) throws NoSuchComponentException, NoUniqueComponentException, ComponentCreationException {
        return getComponent(typeOf(type));
    }

    /// 根据 名称和类型 获取 Component
    default <T> T getComponent(String name, Class<T> type) throws NoSuchComponentException, ComponentCreationException {
        return getComponent(name, typeOf(type));
    }

    /// 根据 类型 获取 Component
    default <T> T getComponent(TypeReference<T> type) throws NoSuchComponentException, NoUniqueComponentException, ComponentCreationException {
        return getComponent(typeOf(type));
    }

    /// 根据 名称和类型 获取 Component
    default <T> T getComponent(String name, TypeReference<T> type) throws NoSuchComponentException, ComponentCreationException {
        return getComponent(name, typeOf(type));
    }

    /// 验证所有 Component 是否可以被成功获取.
    ///
    /// 该方法会主动调用每个 Component 的 getComponent(...).
    /// 对 singleton, 这会创建并缓存实例.
    /// 对 prototype, 这会创建一个临时实例并丢弃.
    default void verifyComponents() throws ComponentCreationException {
        for (var name : componentDefinitions().keySet()) {
            getComponent(name);
        }
    }

}
