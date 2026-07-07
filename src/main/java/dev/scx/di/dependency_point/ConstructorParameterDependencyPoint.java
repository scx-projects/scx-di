package dev.scx.di.dependency_point;

import dev.scx.reflect.ConstructorInfo;
import dev.scx.reflect.ParameterInfo;

/// ConstructorParameterDependencyPoint
///
/// @author scx567888
public record ConstructorParameterDependencyPoint(
    ConstructorInfo constructor,
    ParameterInfo parameter
) implements DependencyPoint {

}
