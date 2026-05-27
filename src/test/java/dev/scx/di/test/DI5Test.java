package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

public class DI5Test {

    public static void main(String[] args) throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        test1();
        test2();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var containerBuilder = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver());

        containerBuilder.registerComponentType("a", A.class);

        //不允许注册一个 接口
        Assert.assertThrows(IllegalComponentTypeException.class, () -> {
            containerBuilder.registerComponentType("b", B.class);
        });

        //不允许注册一个 抽象类
        Assert.assertThrows(IllegalComponentTypeException.class, () -> {
            containerBuilder.registerComponentType("c", C.class);
        });

        //不允许注册一个 注解
        Assert.assertThrows(IllegalComponentTypeException.class, () -> {
            containerBuilder.registerComponentType("d", D.class);
        });

        //不允许非静态成员类
        Assert.assertThrows(IllegalComponentTypeException.class, () -> {
            containerBuilder.registerComponentType("e", E.class);
        });

        containerBuilder.registerComponentType("f", F.class);

        //不允许注册一个 枚举
        Assert.assertThrows(IllegalComponentTypeException.class, () -> {
            containerBuilder.registerComponentType("g", G.class);
        });

        containerBuilder.registerComponentType("h", H.class);

        var container = containerBuilder.build();

        container.verifyComponents();

        A a = container.getComponent(A.class);
        F f = container.getComponent(F.class);
        H h = container.getComponent(H.class);

        //不应该被重复注入 因为是 final 的
        Assert.assertNull(h.a);

        var componentNames = container.componentDefinitions().keySet();
        Assert.assertEquals(componentNames, Set.of("a", "f", "h"));

        // 名称不存在
        Assert.assertThrows(NoSuchComponentException.class, () -> {
            container.getComponent("no");
        });

        // 名称存在 类型不符合
        Assert.assertThrows(NoSuchComponentException.class, () -> {
            container.getComponent("a", B.class);
        });


    }

    @Test
    public static void test2() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var containerBuilder = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver());

        containerBuilder.registerComponentType("a", A.class);

        // 支持字段注入一个 已经存在的类
        var k = new K();
        containerBuilder.registerComponent("k", k, true);

        // 支持直接注册一个已存在的 component
        var a1 = new A();
        containerBuilder.registerComponent("a1", a1);

        Assert.assertThrows(DuplicateComponentNameException.class, () -> {
            containerBuilder.registerComponent("a1", a1);
        });

        var container = containerBuilder.build();

        var a2 = container.getComponent("a1");
        Assert.assertEquals(a1, a2);

        // 现在有多个 A 获取应该报错
        Assert.assertThrows(NoUniqueComponentException.class, () -> {
            container.getComponent(A.class);
        });

        K k1 = container.getComponent(K.class);
        Assert.assertEquals(k, k1);

        Assert.assertEquals(k1.a, a1);
    }

    //枚举
    public enum G {

    }

    //接口
    public interface B {

    }

    //注解
    public @interface D {

    }

    //普通类
    public static class A {

    }

    // record
    public record F(A a) {

    }

    public static class H {

        @Inject
        public final A a;

        public H() {
            this.a = null;
        }

    }

    public static class K {
        @Inject("a1")
        public A a;
    }

    //抽象类
    public abstract class C {

    }

    //内部类
    public class E {

    }

}
