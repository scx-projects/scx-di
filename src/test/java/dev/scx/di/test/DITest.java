package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.ComponentContainerBuilder;
import dev.scx.di.annotation.Inject;
import dev.scx.di.annotation.Value;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import dev.scx.reflect.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class DITest {

    public static void main(String[] args) throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException, NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        test1();
        test2();
    }

    @Test
    public static void test1() throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException, NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var containerBuilder = ComponentContainer.builder()
            .addDependencyResolver(new ValueAnnotationDependencyResolver(new MapValueResolver(Map.of("key1", "Hello", "key2", 12345))))
            .addDependencyResolver(new InjectAnnotationDependencyResolver());

        registerComponents(containerBuilder);

        ComponentContainer container = containerBuilder.build();

        // 测试 1: 单例正常依赖
        A a = container.getComponent(A.class);
        Assert.assertNotNull(a);
        Assert.assertNotNull(a.b);
        Assert.assertNotNull(a.b.c);

        // 测试 2: 字段循环依赖
        D d = container.getComponent(D.class);
        Assert.assertNotNull(d);
        Assert.assertNotNull(d.e);
        Assert.assertNotNull(d.e.f);
        Assert.assertNotNull(d.e.f.d);

        // 测试 3: 构造器循环依赖, 应该抛异常
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(G.class);
        });

        // 测试 4: 多例正常依赖
        J j1 = container.getComponent(J.class);
        J j2 = container.getComponent(J.class);
        Assert.assertNotSame(j1, j2);
        Assert.assertNotNull(j1.k);
        Assert.assertNotNull(j2.k);

        // 测试 5: 多例循环依赖, 应该抛异常
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(M.class);
        });

        // 测试 6: 单例依赖多例
        P p = container.getComponent(P.class);
        Assert.assertNotNull(p);
        Assert.assertNotNull(p.q);

        // 测试 7: 多例依赖单例
        R r1 = container.getComponent(R.class);
        R r2 = container.getComponent(R.class);
        Assert.assertNotSame(r1, r2);
        Assert.assertSame(r1.s, r2.s);

        // 测试 8: 混合注入
        T t = container.getComponent(T.class);
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.u);

        // 测试 9: 无依赖 Component
        W w = container.getComponent(W.class);
        X x = container.getComponent(X.class);
        Assert.assertNotNull(w);
        Assert.assertNotNull(x);

        // 测试 10: @Value 注入
        Y y = container.getComponent(Y.class);
        Z z = container.getComponent(Z.class);
        Assert.assertEquals(y.key1, "Hello");
        Assert.assertEquals(z.key2, 12345);
    }

    @Test
    public static void test2() {
        var containerBuilder = ComponentContainer.builder()
            .addDependencyResolver(new ValueAnnotationDependencyResolver(new MapValueResolver(Map.of("key1", "Hello", "key2", 12345))))
            .addDependencyResolver(new InjectAnnotationDependencyResolver());

        containerBuilder.registerComponentType("UserController", UserController.class);
        containerBuilder.registerComponentType("UserSerivce", UserSerivce.class);
        containerBuilder.registerComponentType("CarSerivce", CarSerivce.class);

        ComponentContainer container = containerBuilder.build();

        var component = container.getComponent(UserController.class);
        var userService = container.getComponent(new TypeReference<BaseService<User>>() {});
        var carService = container.getComponent(CarSerivce.class);

        Assert.assertEquals(component.carService, carService);
        Assert.assertEquals(component.userService, userService);

    }

    private static void registerComponents(ComponentContainerBuilder containerBuilder) throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        // 单例注册
        containerBuilder.registerComponentType("a", A.class);
        containerBuilder.registerComponentType("b", B.class);
        containerBuilder.registerComponentType("c", C.class);
        containerBuilder.registerComponentType("d", D.class);
        containerBuilder.registerComponentType("e", E.class);
        containerBuilder.registerComponentType("f", F.class);
        containerBuilder.registerComponentType("g", G.class);
        containerBuilder.registerComponentType("h", H.class);
        containerBuilder.registerComponentType("i", I.class);
        containerBuilder.registerComponentType("p", P.class);
        containerBuilder.registerComponentType("q", Q.class);
        containerBuilder.registerComponentType("s", S.class);
        containerBuilder.registerComponentType("t", T.class);
        containerBuilder.registerComponentType("u", U.class);
        containerBuilder.registerComponentType("v", V.class);
        containerBuilder.registerComponentType("w", W.class);
        containerBuilder.registerComponentType("x", X.class);
        containerBuilder.registerComponentType("y", Y.class);
        containerBuilder.registerComponentType("z", Z.class);

        // 多例注册
        containerBuilder.registerComponentType("j", J.class, false);
        containerBuilder.registerComponentType("k", K.class, false);
        containerBuilder.registerComponentType("l", L.class, false);
        containerBuilder.registerComponentType("m", M.class, false);
        containerBuilder.registerComponentType("n", N.class, false);
        containerBuilder.registerComponentType("o", O.class, false);
        containerBuilder.registerComponentType("r", R.class, false);
    }

    public static class BaseService<T extends BaseModel> {

    }

    public static class BaseModel {

    }

    public static class User extends BaseModel {

    }

    public static class UserSerivce extends BaseService<User> {

    }

    public static class Car extends BaseModel {

    }

    public static class CarSerivce extends BaseService<Car> {

    }

    public static class UserController {

        public final BaseService<User> userService;

        @Inject
        public BaseService<Car> carService;

        public UserController(BaseService<User> userService) {
            this.userService = userService;
        }

    }

    // =========== 内部静态类（Component定义） ===========

    public static class A {
        @Inject
        public B b;
    }

    public static class B {
        @Inject
        public C c;
    }

    public static class C {
    }

    public static class D {
        @Inject
        public E e;
    }

    public static class E {
        @Inject
        public F f;
    }

    public static class F {
        @Inject
        public D d;
    }

    public static class G {
        public G(H h) {
        }
    }

    public static class H {
        public H(I i) {
        }
    }

    public static class I {
        public I(G g) {
        }
    }

    public static class J {
        @Inject
        public K k;
    }

    public static class K {
        @Inject
        public L l;
    }

    public static class L {
    }

    public static class M {
        @Inject
        public N n;
    }

    public static class N {
        @Inject
        public O o;
    }

    public static class O {
        @Inject
        public M m;
    }

    public static class P {
        @Inject
        public Q q;
    }

    public static class Q {
    }

    public static class R {
        @Inject
        public S s;
    }

    public static class S {
    }

    public static class T {
        @Inject
        public U u;

        public T(V v) {
        }
    }

    public static class U {
    }

    public static class V {
    }

    public static class W {
    }

    public static class X {
    }

    public static class Y {
        @Value("key1")
        public String key1;
    }

    public static class Z {
        public final int key2;

        public Z(@Value("key2") int key2) {
            this.key2 = key2;
        }
    }

}
