package dev.scx.di.assembly;

import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.FieldInfo;
import dev.scx.reflect.TypeInfo;

import java.util.ArrayList;

import static dev.scx.reflect.AccessModifier.PUBLIC;

final class FieldInjectComponentInitializerHelper {

    public static FieldInfo[] findInjectFields(TypeInfo componentType) {
        if (!(componentType instanceof ClassInfo classInfo)) {
            // 不是 ClassInfo 就表示没有 allFields, 直接忽略
            return new FieldInfo[0];
        }

        var fieldInfos = classInfo.allFields();

        var fields = new ArrayList<FieldInfo>();

        // 循环处理 字段
        for (var fieldInfo : fieldInfos) {
            // 只处理非 final 非 static 的 public 字段
            if (fieldInfo.accessModifier() == PUBLIC && !fieldInfo.isFinal() && !fieldInfo.isStatic()) {
                fields.add(fieldInfo);
            }
        }

        return fields.toArray(FieldInfo[]::new);
    }

}
