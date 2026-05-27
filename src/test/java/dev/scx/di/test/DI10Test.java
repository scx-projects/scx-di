package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class DI10Test {

    public static void main(String[] args) throws ComponentCreationException, NoSuchConstructorException, DuplicateComponentNameException, NoSuchComponentException, NoUniqueComponentException, NoUniqueConstructorException, IllegalComponentTypeException {
        test1();
        test2();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new ValueAnnotationDependencyResolver(new MapValueResolver(Map.of())))
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", new TypeReference<A<? extends C>>() {})
            .registerComponentType("b", B.class)
            .registerComponentType("c", C.class)
            .build();

        var component = container.getComponent(A.class);
        Assert.assertNotNull(component);
    }

    @Test
    public static void test2() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new ValueAnnotationDependencyResolver(new MapValueResolver(Map.of())))
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", AAA.class)
            .registerComponentType("b", BBB.class)
            .registerComponentType("c", CCC.class)
            .build();

        var component = container.getComponent(AAA.class);
        Assert.assertNotNull(component);
    }

    public static class A<T extends B> {

        @Inject("c")
        public T a;

        @Inject
        public C c;

    }

    public static class B {

        @Inject
        public C b;

    }

    public static class C extends B {

        @Inject
        public A<? extends C> a;

    }


    public static class AAA {

        public AAA(BBB b) {
            // 这里的 BBB 不允许是半成品,
            // 测试能否 正确获得 ccc
            Assert.assertNotNull(b.ccc);
            Assert.assertEquals(b.ccc.bbb, b);
        }

    }

    public static class BBB {

        @Inject
        public CCC ccc;

    }

    public static class CCC {

        @Inject
        public BBB bbb;

    }

}
