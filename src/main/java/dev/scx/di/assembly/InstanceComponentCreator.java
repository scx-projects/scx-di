package dev.scx.di.assembly;

import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

import static dev.scx.reflect.ScxReflect.typeOf;

/// 实例 组件创建器 (直接返回实例)
///
/// @author scx567888
/// @version 0.0.1
public final class InstanceComponentCreator implements ComponentCreator {

    private final Object component;
    private final TypeInfo componentType;

    public InstanceComponentCreator(Object component) {
        this.component = component;
        this.componentType = typeOf(this.component.getClass());
    }

    @Override
    public Object createComponent(ComponentAssemblyContext context) throws ComponentCreationException {
        return component;
    }

    @Override
    public TypeInfo componentType() {
        return componentType;
    }

}
