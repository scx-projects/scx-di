package dev.scx.di;

import dev.scx.di.dependency_resolver.DependencyResolver;
import dev.scx.di.dependency_resolver.DependencyResolverSelector;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.di.exception.NoSuchComponentException;
import dev.scx.di.exception.NoUniqueComponentException;
import dev.scx.reflect.TypeInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// DefaultComponentContainer
///
/// @author scx567888
/// @version 0.0.1
final class DefaultComponentContainer implements ComponentContainer {

    private final Map<String, ComponentEntity> componentEntities;
    private final DependencyResolverSelector dependencyResolverSelector;
    private final Map<String, ComponentDefinition> componentDefinitions;

    public DefaultComponentContainer(Map<String, ComponentEntity> componentEntities, List<DependencyResolver> dependencyResolvers) {
        this.componentEntities = Map.copyOf(componentEntities);
        this.dependencyResolverSelector = new DependencyResolverSelector(dependencyResolvers);
        this.componentDefinitions = createComponentDefinitions(this.componentEntities);
    }

    private static Map<String, ComponentDefinition> createComponentDefinitions(Map<String, ComponentEntity> componentEntities) {
        var m = new LinkedHashMap<String, ComponentDefinition>();
        for (var e : componentEntities.entrySet()) {
            var k = e.getKey();
            var v = e.getValue();
            m.put(k, new ComponentDefinition(k, v.componentType(), v.isSingleton()));
        }
        return Map.copyOf(m);
    }

    @Override
    public Map<String, ComponentDefinition> componentDefinitions() {
        return componentDefinitions;
    }

    @Override
    public Object getComponent(String name) throws NoSuchComponentException, ComponentCreationException {
        return new DefaultComponentResolutionContext(componentEntities, dependencyResolverSelector).getComponent(name);
    }

    @Override
    public <T> T getComponent(TypeInfo type) throws NoSuchComponentException, NoUniqueComponentException, ComponentCreationException {
        return new DefaultComponentResolutionContext(componentEntities, dependencyResolverSelector).getComponent(type);
    }

    @Override
    public <T> T getComponent(String name, TypeInfo type) throws NoSuchComponentException, ComponentCreationException {
        return new DefaultComponentResolutionContext(componentEntities, dependencyResolverSelector).getComponent(name, type);
    }

}
