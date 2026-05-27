package dev.scx.di;

import dev.scx.di.assembly.ComponentAssemblyContext;
import dev.scx.di.dependency_chain.DependencyChain;
import dev.scx.di.dependency_point.DependencyPoint;
import dev.scx.di.dependency_resolver.DependencyResolverContext;
import dev.scx.di.dependency_resolver.DependencyResolverSelector;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.di.exception.NoSuchComponentException;
import dev.scx.di.exception.NoUniqueComponentException;
import dev.scx.reflect.TypeInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/// 默认 Component 解析上下文.
///
/// @author scx567888
/// @version 0.0.1
final class DefaultComponentResolutionContext implements DependencyResolverContext, ComponentAssemblyContext {

    private final Map<String, ComponentEntity> componentEntities;
    private final DependencyResolverSelector dependencyResolverSelector;
    private final DependencyChain dependencyChain;

    public DefaultComponentResolutionContext(Map<String, ComponentEntity> componentEntities, DependencyResolverSelector dependencyResolverSelector) {
        this.componentEntities = componentEntities;
        this.dependencyResolverSelector = dependencyResolverSelector;
        this.dependencyChain = new DependencyChain();
    }

    @Override
    public Object getComponent(String name) throws NoSuchComponentException, ComponentCreationException {
        var componentEntity = componentEntities.get(name);
        if (componentEntity == null) {
            throw new NoSuchComponentException("未找到任何符合名称的 component, name = [" + name + "]");
        }

        dependencyChain.enterComponent(componentEntity.componentType(), componentEntity.isSingleton());
        try {
            return componentEntity.getComponent(this);
        } finally {
            dependencyChain.exitComponent(componentEntity.componentType(), componentEntity.isSingleton());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getComponent(TypeInfo type) throws NoSuchComponentException, NoUniqueComponentException, ComponentCreationException {
        var list = new ArrayList<ComponentEntity>();
        for (var componentEntity : componentEntities.values()) {
            if (type.isAssignableFrom(componentEntity.componentType())) {
                list.add(componentEntity);
            }
        }
        var size = list.size();
        if (size == 0) {
            throw new NoSuchComponentException("未找到任何符合类型的 component, class = [" + type.rawClass().getName() + "]");
        }
        if (size > 1) {
            throw new NoUniqueComponentException("找到多个符合类型的 component, class = [" + type.rawClass().getName() + "], 已找到 = [" + list.stream().map(c -> c.componentType().rawClass().getName()).collect(Collectors.joining(", ")) + "]");
        }
        var componentEntity = list.get(0);

        dependencyChain.enterComponent(componentEntity.componentType(), componentEntity.isSingleton());
        try {
            return (T) componentEntity.getComponent(this);
        } finally {
            dependencyChain.exitComponent(componentEntity.componentType(), componentEntity.isSingleton());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getComponent(String name, TypeInfo type) throws NoSuchComponentException, ComponentCreationException {
        var componentEntity = componentEntities.get(name);
        if (componentEntity == null) {
            throw new NoSuchComponentException("未找到任何符合名称的 component, name = [" + name + "]");
        }
        if (!type.isAssignableFrom(componentEntity.componentType())) {
            throw new NoSuchComponentException("未找到任何符合名称的 component, name = [" + name + "], class = [" + type.rawClass().getName() + "]");
        }

        dependencyChain.enterComponent(componentEntity.componentType(), componentEntity.isSingleton());
        try {
            return (T) componentEntity.getComponent(this);
        } finally {
            dependencyChain.exitComponent(componentEntity.componentType(), componentEntity.isSingleton());
        }
    }

    /// 解析构造函数参数
    @Override
    public Object resolveDependency(DependencyPoint dependencyPoint) throws Exception {
        var resolver = dependencyResolverSelector.chooseResolver(dependencyPoint);
        if (resolver == null) {
            return null;
        }

        dependencyChain.enterDependency(dependencyPoint);
        try {
            return resolver.resolve(dependencyPoint, this);
        } finally {
            dependencyChain.exitDependency(dependencyPoint);
        }
    }

}
