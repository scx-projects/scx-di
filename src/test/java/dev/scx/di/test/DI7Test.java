package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DI7Test {

    public static void main(String[] args) throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        testConstructorLoop();
        testMixedInjectionLoop();
        testComplexLoop();
        testLazyInjection();
    }

    @Test
    public static void testConstructorLoop() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("x", X.class)
            .registerComponentType("y", Y.class)
            .build();

        // 构造器注入循环, 应该直接抛出异常
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(X.class);
        });
    }

    @Test
    public static void testMixedInjectionLoop() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("m", M.class)
            .registerComponentType("n", N.class)
            .build();

        // 混合构造器注入 + 字段注入, 形成死循环, 应报错
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(M.class);
        });
    }

    @Test
    public static void testComplexLoop() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("p", P.class)
            .registerComponentType("q", Q.class)
            .registerComponentType("r", R.class)
            .registerComponentType("s", S.class)
            .build();

        // 四个Component, 字段注入+构造器注入混合循环
        Assert.assertThrows(ComponentCreationException.class, () -> {
            P component = container.getComponent(P.class);
            System.out.println(component);
        });
    }

    @Test
    public static void testLazyInjection() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("lazyA", LazyA.class)
            .registerComponentType("lazyB", LazyB.class)
            .build();

        // LazyB 延迟注入, 不应引起循环问题
        var lazyA = container.getComponent(LazyA.class);
        Assert.assertNotNull(lazyA.lazyB);
    }

    public static class X {
        public final Y y;

        @Inject
        public X(Y y) {
            this.y = y;
        }
    }

    public static class Y {
        public final X x;

        @Inject
        public Y(X x) {
            this.x = x;
        }
    }

    public static class M {
        public final N n;

        @Inject
        public M(N n) {
            this.n = n;
        }
    }

    public static class N {
        @Inject
        public M m;
    }

    public static class P {
        @Inject
        public Q q;
    }

    public static class Q {
        public final R r;

        @Inject
        public Q(R r) {
            //这里 r 不允许是 半成品对象
            this.r = r;
        }
    }

    public static class R {
        @Inject
        public S s;
    }

    public static class S {
        @Inject
        public P p;
    }

    public static class LazyA {
        @Inject
        public LazyB lazyB;
    }

    public static class LazyB {
        public String doSomething() {
            return "ok";
        }
    }
}

