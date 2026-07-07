package dev.scx.di.assembly;

import dev.scx.di.dependency_point.FieldDependencyPoint;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.FieldInfo;
import dev.scx.reflect.TypeInfo;

import static dev.scx.di.assembly.FieldInjectComponentInitializerHelper.findInjectFields;

/// 字段注入 组件初始化器 (只处理非 final 非 static 的 public 字段)
///
/// @author scx567888
public final class FieldInjectComponentInitializer implements ComponentInitializer {

    private final TypeInfo componentType;
    private final FieldInfo[] fields;

    public FieldInjectComponentInitializer(TypeInfo componentType) {
        this.componentType = componentType;
        this.fields = findInjectFields(this.componentType);
    }

    @Override
    public void initializeComponent(Object component, ComponentAssemblyContext context) throws ComponentCreationException {
        // 循环处理 字段
        for (var field : fields) {
            var point = new FieldDependencyPoint(field);
            try {
                var value = context.resolveDependency(point);
                // 只设置非空值
                if (value != null) {
                    field.set(component, value);
                }
            } catch (Exception e) {
                throw new ComponentCreationException("在类 " + componentType.rawClass().getName() + " 中, 注入字段 [" + field.name() + "] 阶段发生异常 !!!", e);
            }
        }
    }

}
