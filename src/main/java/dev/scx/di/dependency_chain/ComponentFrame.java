package dev.scx.di.dependency_chain;

import dev.scx.reflect.TypeInfo;

/// ComponentFrame
///
/// @author scx567888
record ComponentFrame(
    TypeInfo componentType,
    boolean isSingleton
) implements DependencyChainFrame {

}
