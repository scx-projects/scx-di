package dev.scx.di.dependency_chain;

import dev.scx.di.dependency_point.DependencyPoint;
import dev.scx.di.exception.ComponentCreationException;
import dev.scx.reflect.TypeInfo;

import java.util.ArrayList;

import static dev.scx.di.dependency_chain.DependencyChainHelper.checkDependencyCycle;

/// DependencyChain
///
/// @author scx567888
public final class DependencyChain {

    private final ArrayList<DependencyChainFrame> stack;

    public DependencyChain() {
        this.stack = new ArrayList<>();
    }

    public void enterComponent(TypeInfo componentType, boolean isSingleton) {
        var componentFrame = new ComponentFrame(componentType, isSingleton);

        checkDependencyCycle(stack, componentFrame);

        this.stack.addLast(componentFrame);
    }

    public void exitComponent(TypeInfo componentType, boolean isSingleton) {
        var lastFrame = this.stack.removeLast();
        // 栈平衡检查
        if (lastFrame instanceof ComponentFrame c) {
            if (c.componentType() == componentType && c.isSingleton() == isSingleton) {
                return;
            }
        }
        // 这里几乎不会发生
        throw new IllegalStateException("DependencyChain 栈不平衡: 期望退出 Component [" + componentType.rawClass().getName() + "], 实际栈顶为 [" + lastFrame + "]");
    }

    /// 保存依赖链路
    public void enterDependency(DependencyPoint dependencyPoint) throws ComponentCreationException {
        var dependencyPointFrame = new DependencyPointFrame(dependencyPoint);
        // 直接添加到 链中, 无需检查.
        this.stack.addLast(dependencyPointFrame);
    }

    public void exitDependency(DependencyPoint dependencyPoint) {
        var lastFrame = this.stack.removeLast();
        // 栈平衡检查
        if (lastFrame instanceof DependencyPointFrame d) {
            if (d.dependencyPoint() == dependencyPoint) {
                return;
            }
        }
        // 这里几乎不会发生
        throw new IllegalStateException("DependencyChain 栈不平衡: 期望退出 DependencyPoint [" + dependencyPoint + "], 实际栈顶为 [" + lastFrame + "]");
    }

}
