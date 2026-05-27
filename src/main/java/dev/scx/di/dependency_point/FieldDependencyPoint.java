package dev.scx.di.dependency_point;

import dev.scx.reflect.FieldInfo;

/// FieldDependencyPoint
///
/// @author scx567888
/// @version 0.0.1
public record FieldDependencyPoint(
    FieldInfo field
) implements DependencyPoint {

}
