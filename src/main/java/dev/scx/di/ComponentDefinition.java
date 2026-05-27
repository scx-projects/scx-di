package dev.scx.di;

import dev.scx.reflect.TypeInfo;

/// ComponentDefinition
///
/// @author scx567888
/// @version 0.0.1
public record ComponentDefinition(
    String componentName,
    TypeInfo componentType,
    boolean isSingleton
) {

}
