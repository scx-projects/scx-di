package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DI8Test {

    public static void main(String[] args) throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        testSelfCircularDependency();
        testSimpleCircularDependency();
        testMixedScopeDependency();
        testDeepCircularDependency();
        testConstructorCircularDependencyFail();
        testFieldAndConstructorMixedFail();
        testSingletonDependsOnPrototype();
    }

    @Test
    public static void testSelfCircularDependency() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A.class)
            .build();

        Assert.assertThrows(ComponentCreationException.class, () -> {
            var a = container.getComponent(A.class);
        });
    }

    @Test
    public static void testSimpleCircularDependency() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A1.class)
            .registerComponentType("b", B1.class)
            .build();

        var a = container.getComponent(A1.class);
        var b = container.getComponent(B1.class);

        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.a, a);
    }

    @Test
    public static void testMixedScopeDependency() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A2.class)
            .registerComponentType("b", B2.class, false) // B2 是多例
            .registerComponentType("c", C2.class)
            .build();

        var a = container.getComponent(A2.class);
        var b = container.getComponent(B2.class);
        var c = container.getComponent(C2.class);

        Assert.assertEquals(a.b.c, c);
        Assert.assertEquals(c.a, a);

        Assert.assertNotEquals(container.getComponent(B2.class), b); // 多例检查
    }

    @Test
    public static void testDeepCircularDependency() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A3.class)
            .registerComponentType("b", B3.class)
            .registerComponentType("c", C3.class)
            .registerComponentType("d", D3.class)
            .registerComponentType("e", E3.class)
            .registerComponentType("f", F3.class)
            .build();

        var a = container.getComponent(A3.class);
        var b = container.getComponent(B3.class);
        var c = container.getComponent(C3.class);
        var d = container.getComponent(D3.class);
        var e = container.getComponent(E3.class);
        var f = container.getComponent(F3.class);

        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.c, c);
        Assert.assertEquals(c.d, d);
        Assert.assertEquals(d.e, e);
        Assert.assertEquals(e.f, f);
        Assert.assertEquals(f.a, a); // 环回来
    }

    @Test
    public static void testConstructorCircularDependencyFail() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("x", X4.class)
            .registerComponentType("y", Y4.class)
            .build();

        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(X4.class);
        });
    }

    @Test
    public static void testFieldAndConstructorMixedFail() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("p", P5.class)
            .registerComponentType("q", Q5.class)
            .registerComponentType("r", R5.class)
            .build();

        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(P5.class);
        });
    }

    @Test
    public static void testSingletonDependsOnPrototype() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A6.class)
            .registerComponentType("b", B6.class, false) // B6 多例
            .build();

        var a = container.getComponent(A6.class);

        Assert.assertNotNull(a.b);
        var anotherB = container.getComponent(B6.class);

        Assert.assertNotEquals(a.b, anotherB); // 确保 A 中持有的 B 和容器新取的不一样
    }

    public static class A {

        public A(A a) {

        }

    }

    // 测试类定义们
    public static class A1 {
        @Inject
        public B1 b;
    }

    public static class B1 {
        @Inject
        public A1 a;
    }

    public static class A2 {
        @Inject
        public B2 b;
    }

    public static class B2 {
        @Inject
        public C2 c;
    }

    public static class C2 {
        @Inject
        public A2 a;
    }

    public static class A3 {
        @Inject
        public B3 b;
    }

    public static class B3 {
        @Inject
        public C3 c;
    }

    public static class C3 {
        @Inject
        public D3 d;
    }

    public static class D3 {
        @Inject
        public E3 e;
    }

    public static class E3 {
        @Inject
        public F3 f;
    }

    public static class F3 {
        @Inject
        public A3 a;
    }

    public static class X4 {
        public final Y4 y;

        public X4(Y4 y) {
            this.y = y;
        }
    }

    public static class Y4 {
        public final X4 x;

        public Y4(X4 x) {
            this.x = x;
        }
    }

    public static class P5 {
        @Inject
        public Q5 q;
    }

    public static class Q5 {
        public final R5 r;

        public Q5(R5 r) {
            this.r = r;
        }
    }

    public static class R5 {
        @Inject
        public P5 p;
    }

    public static class A6 {
        @Inject
        public B6 b;
    }

    public static class B6 {
    }
}
