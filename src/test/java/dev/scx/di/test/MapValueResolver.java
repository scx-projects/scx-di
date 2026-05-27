package dev.scx.di.test;

import dev.scx.reflect.TypeInfo;

import java.util.Map;

import static dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver.ValueResolver;

public class MapValueResolver implements ValueResolver {

    private final Map<String, Object> map;

    public MapValueResolver(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object resolveValue(String key, TypeInfo targetType) throws Exception {
        var v = map.get(key);
        if (v == null) {
            throw new IllegalStateException("MissValue: " + key);
        }
        return v;
    }

}
