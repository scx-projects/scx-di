package dev.scx.di;

import dev.scx.di.assembly.ComponentInitializer;
import dev.scx.di.assembly.ConstructorInjectComponentCreator;
import dev.scx.di.assembly.FieldInjectComponentInitializer;
import dev.scx.di.assembly.InstanceComponentCreator;
import dev.scx.di.dependency_resolver.DependencyResolver;
import dev.scx.di.exception.DuplicateComponentNameException;
import dev.scx.di.exception.IllegalComponentTypeException;
import dev.scx.di.exception.NoSuchConstructorException;
import dev.scx.di.exception.NoUniqueConstructorException;
import dev.scx.reflect.TypeInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// DefaultComponentContainerBuilder
///
/// @author scx567888
/// @version 0.0.1
final class DefaultComponentContainerBuilder implements ComponentContainerBuilder {

    private final Map<String, ComponentEntity> componentEntities;
    private final List<DependencyResolver> dependencyResolvers;

    public DefaultComponentContainerBuilder() {
        this.componentEntities = new LinkedHashMap<>();
        this.dependencyResolvers = new ArrayList<>();
    }

    @Override
    public DefaultComponentContainerBuilder registerComponentType(String name, TypeInfo componentType, boolean isSingleton, boolean injectField) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateComponentNameException {
        var creator = new ConstructorInjectComponentCreator(componentType);

        List<ComponentInitializer> initializers = injectField ?
            List.of(new FieldInjectComponentInitializer(creator.componentType())) :
            List.of();

        var componentEntity = isSingleton ?
            new SingletonComponentEntity(creator, initializers) :
            new PrototypeComponentEntity(creator, initializers);

        // 注册, 不允许重复
        var oldValue = componentEntities.putIfAbsent(name, componentEntity);
        if (oldValue != null) {
            throw new DuplicateComponentNameException("重复的 component name, name = [" + name + "]");
        }
        return this;
    }

    @Override
    public DefaultComponentContainerBuilder registerComponent(String name, Object componentInstance, boolean injectField) throws DuplicateComponentNameException {
        var creator = new InstanceComponentCreator(componentInstance);

        List<ComponentInitializer> initializers = injectField ?
            List.of(new FieldInjectComponentInitializer(creator.componentType())) :
            List.of();

        var componentEntity = new SingletonComponentEntity(creator, initializers);

        // 注册, 不允许重复
        var oldValue = componentEntities.putIfAbsent(name, componentEntity);
        if (oldValue != null) {
            throw new DuplicateComponentNameException("重复的 component name, name = [" + name + "]");
        }
        return this;
    }

    @Override
    public DefaultComponentContainerBuilder addDependencyResolver(DependencyResolver dependencyResolver) {
        this.dependencyResolvers.add(dependencyResolver);
        return this;
    }

    @Override
    public ComponentContainer build() {
        return new DefaultComponentContainer(componentEntities, dependencyResolvers);
    }

}
