package dev.scx.di.dependency_point;

import dev.scx.reflect.FieldInfo;

/// FieldDependencyPoint
///
/// @author scx567888
public record FieldDependencyPoint(
    FieldInfo field
) implements DependencyPoint {

}
