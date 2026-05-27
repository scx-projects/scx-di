package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DI3Test {

    public static void main(String[] args) throws ComponentCreationException, NoSuchConstructorException, DuplicateComponentNameException, NoSuchComponentException, NoUniqueComponentException, NoUniqueConstructorException, IllegalComponentTypeException {
        testSingletonFieldCycle();
        testMixedScopeCycle();
        testCrossStageDependency();
    }

    @Test
    public static void testSingletonFieldCycle() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            // 注册单例 Component
            .registerComponentType("A", A.class, true)
            .registerComponentType("B", B.class, true)
            .build();

        A a = container.getComponent(A.class);
        B b = container.getComponent(B.class);

        // 验证循环引用是否为同一实例
        Assert.assertSame(a.b, b);
        Assert.assertSame(b.a, a);
        Assert.assertSame(a.b.a, a); // 多层引用一致性
    }

    @Test
    public static void testMixedScopeCycle() throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException, NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            // 注册不同作用域的 Component
            .registerComponentType("singleton", Singleton.class, true)
            .registerComponentType("prototype", Prototype.class, false) // 多例
            .build();

        Singleton s = container.getComponent(Singleton.class);
        Prototype p1 = s.prototype;
        Prototype p2 = container.getComponent(Prototype.class);

        // 验证作用域
        Assert.assertNotSame(p1, p2); // 多例应不同
        Assert.assertSame(p1.singleton, s); // 原型中的单例引用应为同一实例
        Assert.assertSame(p2.singleton, s);
    }

    @Test
    public static void testCrossStageDependency() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("Alpha", Alpha.class, true)
            .registerComponentType("Beta", Beta.class, true)
            .build();

        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(Alpha.class); // 应检测到循环
        });
    }

    // Component 定义
    public static class A {
        @Inject
        public B b;
    }

    public static class B {
        @Inject
        public A a;
    }

    // Component 定义
    public static class Singleton {
        @Inject
        public Prototype prototype; // 依赖多例
    }

    public static class Prototype {
        @Inject
        public Singleton singleton; // 依赖单例
    }

    // Component 定义
    public static class Alpha {
        public Alpha(Beta beta) {
        } // 构造依赖 Beta
    }

    public static class Beta {
        @Inject
        public Alpha alpha; // 字段依赖 Alpha
    }

}
