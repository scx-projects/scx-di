package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.annotation.Inject;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DI9Test {

    public static void main(String[] args) throws ComponentCreationException, NoSuchConstructorException, DuplicateComponentNameException, NoSuchComponentException, NoUniqueComponentException, NoUniqueConstructorException, IllegalComponentTypeException {
        test1();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException, ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        var container = ComponentContainer.builder()
            .addDependencyResolver(new InjectAnnotationDependencyResolver())
            .registerComponentType("a", A.class)
            .registerComponentType("b", B.class)
            .registerComponentType("c", C.class)
            .registerComponentType("w", W.class)
            .build();

        W component = container.getComponent(W.class);
        Assert.assertNotNull(component);
        Assert.assertNotNull(component.a);
        Assert.assertNotNull(component.b);
        Assert.assertNotNull(component.c);

        Assert.assertEquals(component.a.a, component.b);
        Assert.assertEquals(component.c, component.a.c);
        Assert.assertEquals(component.a, component.c.a);
    }

    public static class A {

        @Inject
        public B a;

        @Inject
        public C c;

    }

    public static class B {

        @Inject
        public C b;

    }

    public static class C {

        @Inject
        public A a;

    }


    public record W(A a, B b, C c) {

    }

}
