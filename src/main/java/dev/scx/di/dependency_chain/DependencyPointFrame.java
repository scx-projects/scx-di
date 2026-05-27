package dev.scx.di.dependency_chain;

import dev.scx.di.dependency_point.DependencyPoint;

/// DependencyPointFrame
///
/// @author scx567888
/// @version 0.0.1
record DependencyPointFrame(
    DependencyPoint dependencyPoint
) implements DependencyChainFrame {

}
