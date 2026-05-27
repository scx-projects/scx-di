package dev.scx.di.assembly;

import dev.scx.di.dependency_point.ConstructorParameterDependencyPoint;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.di.exception.IllegalComponentTypeException;
import dev.scx.di.exception.NoSuchConstructorException;
import dev.scx.di.exception.NoUniqueConstructorException;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ConstructorInfo;
import dev.scx.reflect.TypeInfo;

import java.lang.reflect.InvocationTargetException;

import static dev.scx.di.assembly.ConstructorInjectComponentCreatorHelper.checkType;
import static dev.scx.di.assembly.ConstructorInjectComponentCreatorHelper.findPreferredConstructor;

/// 构造器注入 组件创建器 (根据 ClassInfo 进行反射)
///
/// @author scx567888
/// @version 0.0.1
public final class ConstructorInjectComponentCreator implements ComponentCreator {

    private final ClassInfo componentType;
    private final ConstructorInfo constructor;

    public ConstructorInjectComponentCreator(TypeInfo componentType) throws IllegalComponentTypeException, NoSuchConstructorException, NoUniqueConstructorException {
        this.componentType = checkType(componentType);
        this.constructor = findPreferredConstructor(this.componentType);
    }

    @Override
    public Object createComponent(ComponentAssemblyContext context) throws ComponentCreationException {
        var parameters = constructor.parameters();
        var objects = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i = i + 1) {
            var parameter = parameters[i];
            var point = new ConstructorParameterDependencyPoint(constructor, parameter);
            try {
                objects[i] = context.resolveDependency(point);
            } catch (Exception e) {
                throw new ComponentCreationException("在类 " + componentType.rawClass().getName() + " 中, 解析构造参数 " + parameter.name() + " 时发生异常 ", e);
            }
        }

        try {
            return constructor.newInstance(objects);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ComponentCreationException("在类 " + componentType.rawClass().getName() + " 中, 创建 component 时发生异常 ", e);
        } catch (InvocationTargetException e) {
            throw new ComponentCreationException("在类 " + componentType.rawClass().getName() + " 中, 创建 component 时发生异常 ", e.getCause());
        }
    }

    @Override
    public TypeInfo componentType() {
        return componentType;
    }

}
