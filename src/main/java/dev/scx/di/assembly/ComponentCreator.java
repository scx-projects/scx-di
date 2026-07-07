package dev.scx.di.assembly;

import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

/// ComponentCreator 是容器内部的组件装配阶段抽象之一.
///
/// 注意:
/// ComponentCreator 和 ComponentInitializer 是一组绑定在一起的概念.
/// 它们被拆成两个接口, 不是为了提供两个普通用户扩展点,
/// 而是为了让容器内部能够明确区分组件装配中的两个关键阶段:
///
/// ```text
/// create      : 无对象 -> 原始 Component 实例
/// initialize  : 原始 Component 实例 -> 可用 Component
/// ```
///
/// ComponentCreator 负责第一阶段:
///
/// ```text
/// 无对象 -> 原始 Component 实例
/// ```
///
/// ComponentInitializer 负责第二阶段:
///
/// ```text
/// 原始 Component 实例 -> 初始化完成的 Component
/// ```
///
/// 也就是说, ComponentCreator 和 ComponentInitializer 共同描述了
/// “一个 Component 如何从不存在变成可用对象”的内部装配过程.
///
/// ComponentCreator 返回的对象不保证已经完成初始化.
/// 例如构造器注入完成后, 对象虽然已经被构造出来,
/// 但字段注入、setter 注入、配置值填充、装配回调等初始化逻辑可能尚未执行.
///
/// ComponentInitializer 接收的对象则必须是一个已经存在的 Component 实例.
/// 它可以在这个对象上执行字段注入、setter 注入、配置值填充、装配回调等初始化动作.
///
/// ComponentCreator 不负责:
///
/// - 字段注入
/// - setter 注入
/// - 配置值填充
/// - 装配回调
/// - singleton/prototype 缓存
/// - 循环依赖判断
/// - 半成品对象暴露
/// - CREATING / INITIALIZING / READY 等状态切换
///
/// ComponentInitializer 不负责:
///
/// - 创建对象
/// - 选择构造器
/// - singleton/prototype 缓存
/// - 循环依赖判断
/// - 半成品对象暴露
/// - CREATING / INITIALIZING / READY 等状态切换
///
/// 这些职责属于容器更外层的装配控制逻辑.
///
/// ComponentCreator 和 ComponentInitializer 虽然被抽象成接口,
/// 但它们不是普通用户扩展点.
/// 它们被设计为 sealed interface, 是为了明确表达:
///
/// ```text
/// 组件创建方式和组件初始化方式都属于容器内核控制的装配边界.
/// ```
///
/// 原因是 create 和 initialize 都处在容器状态机的关键边界上.
///
/// 在 create 阶段, 容器必须准确知道对象什么时候从“不存在”变成“已经存在”.
/// 这个时间点会影响:
///
/// - 构造器循环依赖是否可解
/// - 半成品对象是否已经可以被记录
/// - singleton 缓存何时可以保存实例
/// - 后续 INITIALIZING 阶段是否可以 early expose
/// - 错误链路是否能正确指向具体依赖点
///
/// 在 initialize 阶段, 对象已经创建出来, 但还没有完成初始化.
/// 对于字段注入形成的循环依赖, 容器可能只允许在这个阶段暴露半成品对象.
/// 因此 initialize 阶段会影响:
///
/// - 字段循环依赖是否可解
/// - 半成品对象是否允许作为 early reference 返回
/// - 初始化阶段的依赖是否能被记录为 DependencyPoint
/// - 解析失败时错误是否能准确指向字段或参数
/// - 状态机、缓存和 early reference 逻辑是否保持一致
///
/// 因此, ComponentCreator 和 ComponentInitializer 的实现都必须遵守容器的依赖解析规则:
///
/// ```text
/// 如果 create 或 initialize 阶段需要依赖其他值,
/// 必须通过 context.resolveDependency(DependencyPoint) 解析.
/// ```
///
/// 例如构造器注入时, ConstructorInjectComponentCreator 会为每个构造参数生成
/// ConstructorParameterDependencyPoint, 然后调用:
///
/// ```text
/// context.resolveDependency(dependencyPoint)
/// ```
///
/// 例如字段注入时, FieldInjectComponentInitializer 会为每个字段生成
/// FieldDependencyPoint, 然后调用:
///
/// ```text
/// context.resolveDependency(dependencyPoint)
/// ```
///
/// 这样容器才能知道:
///
/// - 当前正在解析哪个构造参数或字段
/// - 该依赖属于构造器依赖还是字段依赖
/// - 依赖链路中是否出现循环
/// - 循环依赖发生在 create 阶段还是 initialize 阶段
/// - 该循环依赖是否可解
/// - 解析失败时应该把错误挂到哪个依赖点上
///
/// 实现类可以通过 context.resolveDependency(...) 解析 create 或 initialize 阶段所需的依赖,
/// 但不应该绕过 context 直接访问容器内部结构.
/// 也不应该自行调用底层 getComponent(...)、自行读取配置、或自行解释 @Inject / @Value 等依赖注解.
///
/// 如果允许用户任意实现 ComponentCreator 或 ComponentInitializer,
/// 用户实现很容易绕过统一依赖解析链路:
///
/// ```text
/// DependencyPoint -> context.resolveDependency(...) -> DependencyResolver
/// ```
///
/// 一旦绕过这条链路, 依赖解析路径就会分裂:
///
/// ```text
/// 路径一:
/// DependencyPoint -> resolveDependency(...) -> DependencyResolver
///
/// 路径二:
/// 自定义 Creator / Initializer 自己解析依赖并用于创建或初始化对象
/// ```
///
/// 这样 DependencyResolver 就不再是唯一的依赖解析扩展点.
/// 容器也无法可靠维护依赖链、循环检测、错误上下文、状态机、
/// 缓存一致性和 early reference 逻辑.
///
/// 因此:
///
/// ```text
/// ComponentCreator 和 ComponentInitializer 是内部架构边界, 不是普通用户扩展点.
/// ```
///
/// 换句话说:
///
/// ```text
/// ComponentCreator 负责发现/产生创建阶段的依赖点, 并通过 context 解析它们.
/// ComponentInitializer 负责发现/产生初始化阶段的依赖点, 并通过 context 解析它们.
/// DependencyResolver 负责解释这些依赖点应该解析成什么值.
/// ```
///
/// 这种分工可以保证所有依赖解析都经过统一链路:
///
/// ```text
/// DependencyPoint -> context.resolveDependency(...) -> DependencyResolver
/// ```
///
/// 用户如果想扩展“某个字段、参数、注解如何被解析成值”,
/// 应该扩展 DependencyResolver,
/// 而不是实现新的 ComponentCreator 或 ComponentInitializer.
///
/// 当前允许的创建方式由容器显式控制:
///
/// - ConstructorInjectComponentCreator
/// - InstanceComponentCreator
///
/// 当前允许的初始化方式由容器显式控制:
///
/// - FieldInjectComponentInitializer
///
/// @author scx567888
public sealed interface ComponentCreator permits ConstructorInjectComponentCreator, InstanceComponentCreator {

    /// 创建或取得一个原始 Component 实例.
    ///
    /// 返回值不保证已经完成初始化.
    ///
    /// 实现类可以通过 context.resolveDependency(...) 解析创建阶段所需的依赖,
    /// 但不应该绕过 context 直接访问容器内部结构.
    ///
    /// @param context 当前组件解析上下文
    /// @return 原始 Component 实例, 不保证已经完成初始化
    /// @throws ComponentCreationException 创建 Component 失败时抛出
    Object createComponent(ComponentAssemblyContext context) throws ComponentCreationException;

    /// 返回该 Creator 能够创建的 Component 类型.
    ///
    /// 该方法只提供类型元信息, 不应触发组件创建.
    ///
    /// @return Component 类型
    TypeInfo componentType();

}
