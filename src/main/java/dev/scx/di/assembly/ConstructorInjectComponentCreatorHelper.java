package dev.scx.di.assembly;

import dev.scx.di.annotation.Inject;
import dev.scx.di.exception.IllegalComponentTypeException;
import dev.scx.di.exception.NoSuchConstructorException;
import dev.scx.di.exception.NoUniqueConstructorException;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ConstructorInfo;
import dev.scx.reflect.TypeInfo;

import java.util.ArrayList;
import java.util.List;

import static dev.scx.reflect.AccessModifier.PUBLIC;
import static dev.scx.reflect.ClassKind.*;

final class ConstructorInjectComponentCreatorHelper {

    public static ClassInfo checkType(TypeInfo typeInfo) throws IllegalComponentTypeException {
        if (!(typeInfo instanceof ClassInfo classInfo)) {
            throw new IllegalComponentTypeException("componentType " + typeInfo.rawClass().getName() + " 不支持非普通类 ");
        }
        var classType = classInfo.classKind();
        if (classType == CLASS) {
            if (classInfo.isMemberClass() && !classInfo.isStatic()) {
                throw new IllegalComponentTypeException("componentType " + typeInfo.rawClass().getName() + " 不支持非静态的成员类 ");
            }
            if (classInfo.isAbstract()) {
                throw new IllegalComponentTypeException("componentType " + typeInfo.rawClass().getName() + " is an abstract class");
            }
        }
        if (classType == INTERFACE) {
            throw new IllegalComponentTypeException("componentType " + typeInfo.rawClass().getName() + " is an interface");
        }
        if (classType == ANNOTATION) {
            throw new IllegalComponentTypeException("componentType " + typeInfo.rawClass().getName() + " is an annotation");
        }
        if (classType == ENUM) {
            throw new IllegalComponentTypeException("componentType " + typeInfo.rawClass().getName() + " is an enum");
        }
        return classInfo;
    }

    /// 查找构造函数
    public static ConstructorInfo findPreferredConstructor(ClassInfo classInfo) throws NoSuchConstructorException, NoUniqueConstructorException {
        // 我们只使用 public 的 构造函数
        var publicConstructors = new ArrayList<ConstructorInfo>();
        for (var constructor : classInfo.constructors()) {
            if (constructor.accessModifier() == PUBLIC) {
                publicConstructors.add(constructor);
            }
        }

        // 一个都没有 报错
        if (publicConstructors.isEmpty()) {
            throw new NoSuchConstructorException("无法找到类 " + classInfo.rawClass().getName() + " 的任何 public 构造方法," + "至少需要一个 public 构造方法用于创建 Component.");
        }

        // 只找到一个直接用
        if (publicConstructors.size() == 1) {
            return publicConstructors.get(0);
        }

        // 找到多个 需要查看是否有且只有一个标注了 Inject 注解的
        var preferredConstructors = new ArrayList<ConstructorInfo>();
        for (var constructorInfo : publicConstructors) {
            if (constructorInfo.findAnnotation(Inject.class) != null) {
                preferredConstructors.add(constructorInfo);
            }
        }
        if (preferredConstructors.isEmpty()) {
            throw new NoUniqueConstructorException(
                "在类 " + classInfo.rawClass().getName() + " 中检测到多个 public 构造方法, 且都未标注 @Inject 注解," +
                    "无法确定应使用哪个构造方法. \n" +
                    "可用的 public 构造方法列表: \n" + formatConstructors(publicConstructors) +
                    "\n请在期望使用的构造方法上添加 @Inject 注解. "
            );
        }
        if (preferredConstructors.size() == 1) {
            return preferredConstructors.get(0);
        }

        throw new NoUniqueConstructorException(
            "在类 " + classInfo.rawClass().getName() + " 中检测到多个标注了 @Inject 注解的 public 构造方法, " +
                "无法唯一确定使用哪个构造方法. \n" +
                "冲突的构造方法列表: \n" + formatConstructors(preferredConstructors) +
                "\n同一个类中只能有一个构造方法标注 @Inject, 请检查修正."
        );

    }

    /// 美化构造函数输出
    private static String formatConstructors(List<ConstructorInfo> constructors) {
        var builder = new StringBuilder();
        for (var constructor : constructors) {
            builder.append("  - ")
                .append(constructor.rawConstructor().toGenericString())
                .append("\n");
        }
        return builder.toString();
    }

}
