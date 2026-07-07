package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class DI4Test {

    public static void main(String[] args) throws ComponentCreationException, NoSuchConstructorException, DuplicateComponentNameException, NoSuchComponentException, NoUniqueComponentException, NoUniqueConstructorException, IllegalComponentTypeException {
        test1();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var containerBuilder = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .addDependencyResolver(new ValueAnnotationDependencyResolver(new MapValueResolver(Map.of("key1", "Hello", "key2", "12345"))));

        containerBuilder.registerComponentType("a", A.class);
        containerBuilder.registerComponentType("b", B.class);
        // 注册阶段就会报错
        Assert.expectThrows(NoUniqueConstructorException.class, () -> {
            containerBuilder.registerComponentType("c", C.class);
        });
        containerBuilder.registerComponentType("d", D.class);
        Assert.expectThrows(NoSuchConstructorException.class, () -> {
            containerBuilder.registerComponentType("e", E.class);
        });
        Assert.expectThrows(NoUniqueConstructorException.class, () -> {
            containerBuilder.registerComponentType("f", F.class);
        });

        var container = containerBuilder.build();

        //正常获取
        A a = container.getComponent(A.class);
        B b = container.getComponent(B.class);

        Assert.expectThrows(NoSuchComponentException.class, () -> {
            C c = container.getComponent(C.class);
        });

        container.getComponent(D.class);
        Assert.expectThrows(NoSuchComponentException.class, () -> {
            container.getComponent(E.class);
        });
        Assert.expectThrows(NoSuchComponentException.class, () -> {
            container.getComponent(F.class);
        });
    }

    public static class A {

    }

    public static class B {
        public B() {
        }
    }

    public static class C {
        public C() {
        }

        public C(int a) {
        }
    }

    public static class D {

        @Inject
        public D() {

        }

        public D(int a) {

        }

    }

    public static class E {
        private E() {

        }
    }

    public static class F {
        @Inject
        public F() {

        }

        @Inject
        public F(int a) {

        }
    }

}
