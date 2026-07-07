package dev.scx.di;

import dev.scx.di.assembly.ComponentAssemblyContext;
import dev.scx.di.assembly.ComponentCreator;
import dev.scx.di.assembly.ComponentInitializer;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

import java.util.List;

/// PrototypeComponentEntity
///
/// @author scx567888
final class PrototypeComponentEntity implements ComponentEntity {

    private final ComponentCreator creator;
    private final List<ComponentInitializer> initializers;

    public PrototypeComponentEntity(ComponentCreator creator, List<ComponentInitializer> initializers) {
        this.creator = creator;
        this.initializers = initializers;
    }

    @Override
    public Object getComponent(ComponentAssemblyContext context) throws ComponentCreationException {
        // 1, 创建组件
        var component = creator.createComponent(context);
        // 2, 初始化组件
        for (var initializer : initializers) {
            initializer.initializeComponent(component, context);
        }
        // 3, 返回组件
        return component;
    }

    @Override
    public TypeInfo componentType() {
        return creator.componentType();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
