package dev.scx.di.dependency_chain;

import dev.scx.di.dependency_point.ConstructorParameterDependencyPoint;
import dev.scx.di.dependency_point.DependencyPoint;
import dev.scx.di.dependency_point.FieldDependencyPoint;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

import java.util.ArrayList;
import java.util.List;

/// DependencyChainHelper
///
/// @author scx567888
final class DependencyChainHelper {

    public static void checkDependencyCycle(ArrayList<DependencyChainFrame> stack, ComponentFrame componentFrame) {
        // 1, 提取循环依赖链条 若循环依赖链条为空 则表示没有循环依赖
        var circularDependencyChain = extractCircularDependencyChain(stack, componentFrame.componentType());

        // 没有循环依赖, 无需后续检查
        if (circularDependencyChain == null) {
            return;
        }

        // 2, 检查是否是不可解决的循环依赖
        var unsolvableCycleType = isUnsolvableCycle(circularDependencyChain);

        // 可解决, 直接返回
        if (unsolvableCycleType == null) {
            return;
        }

        // 3, 创建友好的错误提示
        var message = buildCycleMessage(stack, componentFrame.componentType());
        var why = switch (unsolvableCycleType) {
            case CONSTRUCTOR -> "构造函数循环依赖";
            case ALL_PROTOTYPE -> "多例循环依赖";
        };

        throw new ComponentCreationException("在创建类 " + componentFrame.componentType() + " 时, 检测到无法解决的" + why + ": \n\n" + message);
    }

    /// 查找 循环开始的索引
    private static int findCycleStartIndex(List<DependencyChainFrame> stack, TypeInfo componentType) {
        // 这里正序倒序本质都一样 因为我们每次 入栈都会检查, 但大多数情况 循环链 都是更偏向末尾, 这里倒序可能会快一点.
        for (int i = stack.size() - 1; i >= 0; i = i - 1) {
            var frame = stack.get(i);
            if (frame instanceof ComponentFrame c && c.componentType() == componentType) {
                return i;
            }
        }
        return -1;
    }

    /// 提取循环依赖链条.
    ///
    /// 返回 null 表示没有循环链
    private static List<DependencyChainFrame> extractCircularDependencyChain(List<DependencyChainFrame> stack, TypeInfo componentType) {
        var cycleStartIndex = findCycleStartIndex(stack, componentType);
        if (cycleStartIndex == -1) {
            return null;
        } else {
            // 此处无需拼接 componentType
            return stack.subList(cycleStartIndex, stack.size());
        }
    }

    /// 是否是无法解决的循环
    ///
    /// 返回 null 表示可以解决
    private static UnsolvableCycleType isUnsolvableCycle(List<DependencyChainFrame> circularDependencyChain) {
        // 1, 检查链路中是否有构造器注入类型的依赖, 构造器注入 => 无法解决
        // 确实在某些情况下 如: A 类 构造器注入 b, B 类 字段注入 a,
        // 我们可以通过先创建 半成品 b, 再创建 a, 然后再 b.a = a 来完成创建
        // 但这违反了一个规则 及 构造函数中拿到的永远应该是一个 完整对象 而不是半成品 因为用户有可能在 A 的构造函数中, 调用 b.a
        // 此时因为 b 是一个半成品对象, 便会引发空指针, 所以我们从根源上禁止 任何链路上存在 构造器循环依赖

        for (var c : circularDependencyChain) {
            if (c instanceof DependencyPointFrame d) {
                if (d.dependencyPoint() instanceof ConstructorParameterDependencyPoint) {
                    return UnsolvableCycleType.CONSTRUCTOR;// 无法解决
                }
            }
        }

        // 2, 此时严格来说整个循环链条中 全部都是 字段注入
        // 关于 字段注入 严格来说 只要整个链条中存在任意一个单例对象 便可以打破无限循环
        // 所以我们在此处进行 检测 整个链路是否存在任意一个单例

        for (var c : circularDependencyChain) {
            if (c instanceof ComponentFrame f) {
                if (f.isSingleton()) {
                    return null; // 只要存在单例 就表示能够解决
                }
            }
        }

        // 3, 如果链路中没有单例（只有多例）, 无法解决循环依赖
        return UnsolvableCycleType.ALL_PROTOTYPE;
    }

    /// 构建循环链的错误信息
    /// 这里 stack 我们用完整的链 保证输出信息的完整.
    private static String buildCycleMessage(ArrayList<DependencyChainFrame> stack, TypeInfo componentType) {
        // 1. 找到循环起始点
        var cycleStartIndex = findCycleStartIndex(stack, componentType);

        // 在一次有效的依赖解析路径中:
        // ComponentFrame 和 DependencyPointFrame 一定交错出现.
        // 循环起始 ComponentFrame 后的下一个 frame, 就是该循环第一条依赖边.
        // 所以这里 + 1. 因为我们要 展示的是 DependencyPointFrame
        cycleStartIndex = cycleStartIndex + 1;

        // 2. 构建可视化链条
        var sb = new StringBuilder();

        ComponentFrame currentComponentFrame = null;

        for (int i = 0; i < stack.size(); i = i + 1) {
            var frame = stack.get(i);

            if (frame instanceof ComponentFrame c) {
                currentComponentFrame = c;
                continue;
            }

            // 这里因为密封类 只可能是 DependencyPointFrame, 强转安全.
            var dependencyPointFrame = (DependencyPointFrame) frame;

            if (currentComponentFrame == null) {
                // 这里理论不可能发生错误.
                throw new IllegalStateException("DependencyChain 结构错误: DependencyPointFrame 前没有 ComponentFrame");
            }

            var baseInfo = currentComponentFrame.componentType().rawClass().getName() + " " + getDependencyDescription(dependencyPointFrame.dependencyPoint()) + "\n";

            if (i < cycleStartIndex) { // 不处于循环中
                sb.append("    ").append(baseInfo);
                sb.append("              🡻\n");
            } else if (i == cycleStartIndex) {// 循环开始
                sb.append("╭─➤ ").append(baseInfo);
                sb.append("|             🡻\n");
                // 循环结束 换句话说 起始等于结束 所以是自我引用
                if (i == stack.size() - 1) {
                    sb.append("╰───────── (自我引用) \n");
                }
            } else if (i < stack.size() - 1) {// 循环节点
                sb.append("|   ").append(baseInfo);
                sb.append("|             🡻\n");
            } else { // 闭环
                sb.append("╰── ").append(baseInfo);
            }
        }

        return sb.toString();
    }

    private static String getDependencyDescription(DependencyPoint dependencyPoint) {
        return switch (dependencyPoint) {
            case ConstructorParameterDependencyPoint c -> "(构造参数: " + c.parameter().name() + ")";
            case FieldDependencyPoint f -> "[字段: " + f.field().name() + "]";
        };
    }

    public enum UnsolvableCycleType {

        CONSTRUCTOR,

        ALL_PROTOTYPE

    }

}
