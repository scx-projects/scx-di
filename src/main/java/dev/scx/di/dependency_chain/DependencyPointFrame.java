package dev.scx.di.dependency_chain;

import dev.scx.di.dependency_point.DependencyPoint;

/// DependencyPointFrame
///
/// @author scx567888
record DependencyPointFrame(
    DependencyPoint dependencyPoint
) implements DependencyChainFrame {

}
