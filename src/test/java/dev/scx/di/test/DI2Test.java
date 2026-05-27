package dev.scx.di.test;

import dev.scx.di.ComponentContainer;
import dev.scx.di.ComponentContainerBuilder;
import dev.scx.di.annotation.Inject;
import dev.scx.di.annotation.Value;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;
import dev.scx.di.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class DI2Test {

    public static void main(String[] args) throws ComponentCreationException, NoSuchConstructorException, NoSuchComponentException, DuplicateComponentNameException, NoUniqueComponentException, NoUniqueConstructorException, IllegalComponentTypeException {
        test2();
    }

    @Test
    public static void test2() throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException, NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        var containerBuilder = ComponentContainer.builder()
            .addDependencyResolver(new ValueAnnotationDependencyResolver(new MapValueResolver(Map.of("existKey", "value"))))
            .addDependencyResolver(new InjectAnnotationDependencyResolver());

        registerExtremeComponents(containerBuilder); // 注册极端测试所需的 Component

        var container = containerBuilder.build();

        testMultiLevelCycleDependency(container);
        testMixedScopeCycleDependency(container);
        testConstructorFieldMixedCycle(container);
        testPrototypeCycleDependency(container);
        testMissingValueInjection(container);
        testMultipleImplInjection(container);
        testComponentCreationException(container);
    }

    //============= 测试用例方法 =============

    /// 测试多层嵌套循环依赖 (单例)
    /// A → B → C → A
    /// 预期: 由于是字段注入, 应成功解决循环依赖
    private static void testMultiLevelCycleDependency(ComponentContainer container) throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        MultiA a = container.getComponent(MultiA.class);
        Assert.assertNotNull(a);
        Assert.assertSame(a.b.c.a, a); // 检查循环引用是否为同一实例
    }

    /// 测试混合作用域的循环依赖
    /// 单例 SingletonM → 原型 PrototypeM → 单例 SingletonM
    /// 预期: 由于原型每次创建新实例, 不会形成真正的循环, 应成功创建
    private static void testMixedScopeCycleDependency(ComponentContainer container) throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        SingletonM s = container.getComponent(SingletonM.class);
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.prototype);
        // 验证原型实例不同
        Assert.assertNotSame(s.prototype, container.getComponent(PrototypeM.class));
    }

    /// 测试构造器 + 字段混合循环依赖
    /// ConstructorCycleX 需要 ConstructorCycleY 作为构造参数
    /// ConstructorCycleY 需要 ConstructorCycleX 作为字段注入
    /// 预期: 抛出 ComponentCreationException
    private static void testConstructorFieldMixedCycle(ComponentContainer container) {
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(ConstructorCycleY.class);
        });
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(ConstructorCycleX.class);
        });
    }

    /// 测试原型 Component 之间的循环依赖
    /// PrototypeP → PrototypeQ → PrototypeP
    /// 预期: 每次获取都会抛出异常
    private static void testPrototypeCycleDependency(ComponentContainer container) {
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(PrototypeP.class);
        });
    }

    /// 测试 @Value 注入不存在的配置项
    /// 预期: 抛出 InjectionException
    private static void testMissingValueInjection(ComponentContainer container) {
        Assert.assertThrows(ComponentCreationException.class, () -> {
            container.getComponent(MissingValue.class);
        });
    }

    /// 测试同一接口多个实现的注入问题
    /// 使用 @Named 或 @Qualifier 解决歧义
    /// 预期: 正确注入指定名称的 Component
    private static void testMultipleImplInjection(ComponentContainer container) throws ComponentCreationException, NoSuchComponentException, NoUniqueComponentException {
        MultiServiceUser user = container.getComponent(MultiServiceUser.class);
        Assert.assertTrue(user.service instanceof PrimaryServiceImpl);
    }

    /// 测试 Component 创建时抛出异常的情况
    /// 预期: 包装成 ComponentCreationException 并传递原始异常
    private static void testComponentCreationException(ComponentContainer container) {
        Throwable cause = Assert.expectThrows(ComponentCreationException.class, () -> {
            container.getComponent(ErrorComponent.class);
        });

        Assert.assertTrue(cause.getCause() instanceof IllegalStateException);
    }

    //============= 注册极端测试 Component =============
    private static void registerExtremeComponents(ComponentContainerBuilder containerBuilder) throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentTypeException {
        // 多层循环依赖（单例）
        containerBuilder.registerComponentType("multiA", MultiA.class);
        containerBuilder.registerComponentType("multiB", MultiB.class);
        containerBuilder.registerComponentType("multiC", MultiC.class);

        // 混合作用域循环
        containerBuilder.registerComponentType("singletonM", SingletonM.class);
        containerBuilder.registerComponentType("prototypeM", PrototypeM.class, false);

        // 构造器+字段混合循环
        containerBuilder.registerComponentType("cycleX", ConstructorCycleX.class);
        containerBuilder.registerComponentType("cycleY", ConstructorCycleY.class);

        // 原型循环依赖
        containerBuilder.registerComponentType("protoP", PrototypeP.class, false);
        containerBuilder.registerComponentType("protoQ", PrototypeQ.class, false);

        // 缺失配置项测试
        containerBuilder.registerComponentType("missingValue", MissingValue.class);

        // 多实现测试
        containerBuilder.registerComponentType("primaryService", PrimaryServiceImpl.class);
        containerBuilder.registerComponentType("secondaryService", SecondaryServiceImpl.class);
        containerBuilder.registerComponentType("multiServiceUser", MultiServiceUser.class);

        // 异常 Component
        containerBuilder.registerComponentType("errorComponent", ErrorComponent.class);
    }

    //============= 极端测试 Component 定义 =============

    // 多实现测试
    public interface MultiService {}

    // 多层循环依赖
    public static class MultiA {
        @Inject
        public MultiB b;
    }

    public static class MultiB {
        @Inject
        public MultiC c;
    }

    public static class MultiC {
        @Inject
        public MultiA a;
    }

    // 混合作用域循环
    public static class SingletonM {
        @Inject
        public PrototypeM prototype;
    }

    public static class PrototypeM {
        @Inject
        public SingletonM singleton; // 依赖单例不会形成真正循环
    }

    // 构造器+字段混合循环
    public static class ConstructorCycleX {
        // 理论上这个是创建不出来的 因为 构造函数中不应该存在半成品对象
        public ConstructorCycleX(ConstructorCycleY y) {
            Assert.assertNotNull(y.x);
        }
    }

    public static class ConstructorCycleY {
        @Inject
        public ConstructorCycleX x;
    }

    // 原型循环依赖
    public static class PrototypeP {
        @Inject
        public PrototypeQ q;
    }

    public static class PrototypeQ {
        @Inject
        public PrototypeP p;
    }

    // @Value 缺失配置
    public static class MissingValue {
        @Value("notExistKey")
        public String val;
    }

    public static class PrimaryServiceImpl implements MultiService {}

    public static class SecondaryServiceImpl implements MultiService {}

    public static class MultiServiceUser {
        @Inject("primaryService")
        public MultiService service; // 需通过名称或 @Primary 解决
    }

    // 异常 Component
    public static class ErrorComponent {
        public ErrorComponent() {
            throw new IllegalStateException("模拟构造异常");
        }
    }

}
