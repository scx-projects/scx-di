package dev.scx.di;

import dev.scx.di.assembly.ComponentAssemblyContext;
import dev.scx.di.assembly.ComponentCreator;
import dev.scx.di.assembly.ComponentInitializer;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

import java.util.List;

import static dev.scx.di.SingletonComponentEntity.ComponentStatus.*;

/// SingletonComponentEntity
///
/// @author scx567888
/// @version 0.0.1
final class SingletonComponentEntity implements ComponentEntity {

    private final ComponentCreator creator;
    private final List<ComponentInitializer> initializers;

    private ComponentStatus componentStatus;
    private Object component;

    public SingletonComponentEntity(ComponentCreator creator, List<ComponentInitializer> initializers) {
        this.creator = creator;
        this.initializers = initializers;
        this.componentStatus = ComponentStatus.NULL;
        this.component = null;
    }

    @Override
    public Object getComponent(ComponentAssemblyContext context) throws ComponentCreationException {
        // 已完成状态 直接返回
        if (componentStatus == READY) {
            return component;
        }

        // 半注入状态 返回早期对象
        if (componentStatus == INITIALIZING) {
            return component;
        }

        // 创建状态
        if (componentStatus == CREATING) {
            // 这里理论上不可能发生
            throw new ComponentCreationException("在创建类 " + this.componentType() + " 时, 内部状态错误");
        }

        // 这里注意状态机回滚
        try {
            // 1, 创建组件
            componentStatus = CREATING;
            component = creator.createComponent(context);
        } catch (ComponentCreationException e) {
            componentStatus = NULL;
            component = null;
            throw e;
        }

        // 这里注意状态机回滚
        try {
            // 2, 初始化组件
            componentStatus = INITIALIZING;
            for (var initializer : initializers) {
                initializer.initializeComponent(component, context);
            }
        } catch (ComponentCreationException e) {
            // 这里必须回退到 NULL 因为 component 当前已经是一个脏对象了 必须丢弃.
            componentStatus = NULL;
            component = null;
            throw e;
        }

        // 3, 彻底完成 返回组件
        componentStatus = READY;
        return component;
    }

    @Override
    public TypeInfo componentType() {
        return creator.componentType();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public enum ComponentStatus {
        NULL,         // 未开始
        CREATING,     // 正在创建
        INITIALIZING, // 正在初始化
        READY         // 完全就绪
    }

}
