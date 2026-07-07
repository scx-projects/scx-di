package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DI6Test {

    public static void main(String[] args) throws ComponentCreationException, NoSuchConstructorException, DuplicateComponentNameException, NoSuchComponentException, NoUniqueComponentException, NoUniqueConstructorException, IllegalComponentTypeException {
        test1();
        test2();
        test3();
        test4();
        test5();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A.class)
            .registerComponentType("b", B.class)
            .registerComponentType("c", C.class)
            .registerComponentType("d", D.class)
            .registerComponentType("e", E.class)
            .build();

        var a = container.getComponent(A.class);
        var b = container.getComponent(B.class);
        var c = container.getComponent(C.class);
        var d = container.getComponent(D.class);
        var e = container.getComponent(E.class);

        //单例循环依赖 可以解决
        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.c, c);
        Assert.assertEquals(c.d, d);
        Assert.assertEquals(d.e, e);
        Assert.assertEquals(e.a, a);

    }

    @Test
    public static void test2() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A.class, false)
            .registerComponentType("b", B.class)
            .registerComponentType("c", C.class)
            .registerComponentType("d", D.class)
            .registerComponentType("e", E.class)
            .build();

        var a = container.getComponent(A.class);
        var b = container.getComponent(B.class);
        var c = container.getComponent(C.class);
        var d = container.getComponent(D.class);
        var e = container.getComponent(E.class);

        //循环链中至少有一个单例 可以解决
        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.c, c);
        Assert.assertEquals(c.d, d);
        Assert.assertEquals(d.e, e);

        //因为 a 是多例的 这里应该不同
        Assert.assertNotEquals(e.a, a);

    }

    @Test
    public static void test3() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A.class, false)
            .registerComponentType("b", B.class, false)
            .registerComponentType("c", C.class, false)
            .registerComponentType("d", D.class, false)
            .registerComponentType("e", E.class)
            .build();

        var a = container.getComponent(A.class);
        var b = container.getComponent(B.class);
        var c = container.getComponent(C.class);
        var d = container.getComponent(D.class);
        var e = container.getComponent(E.class);

        //循环链中至少有一个单例 可以解决
        Assert.assertNotEquals(a.b, b);
        Assert.assertNotEquals(b.c, c);
        Assert.assertNotEquals(c.d, d);

        //只有 e 是单例的 这里应该相同
        Assert.assertEquals(d.e, e);

        Assert.assertNotEquals(e.a, a);

    }

    @Test
    public static void test4() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A.class, false)
            .registerComponentType("b", B.class, false)
            .registerComponentType("c", C.class, false)
            .registerComponentType("d", D.class, false)
            .registerComponentType("e", E.class, false)
            .build();

        //全是多例 不可解决
        Assert.assertThrows(ComponentCreationException.class, () -> {
            var a = container.getComponent(A.class);
        });
        Assert.assertThrows(ComponentCreationException.class, () -> {
            var b = container.getComponent(B.class);
        });
        Assert.assertThrows(ComponentCreationException.class, () -> {
            var c = container.getComponent(C.class);
        });
        Assert.assertThrows(ComponentCreationException.class, () -> {
            var d = container.getComponent(D.class);
        });
        Assert.assertThrows(ComponentCreationException.class, () -> {
            var e = container.getComponent(E.class);
        });

    }

    @Test
    public static void test5() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("w", W.class)
            .registerComponentType("t", T.class)
            .build();

        Assert.assertThrows(ComponentCreationException.class, () -> {
            W component = container.getComponent(W.class);
        });

    }

    public static class A {

        @Inject
        public B b;

    }

    public static class B {

        @Inject
        public C c;

    }

    public static class C {

        @Inject
        public D d;

    }

    public static class D {

        @Inject
        public E e;

    }

    public static class E {

        @Inject
        public A a;

    }

    public static class W {
        public W(T t) {

        }
    }

    public static class T {
        public T(T t1) {
        }
    }

}
