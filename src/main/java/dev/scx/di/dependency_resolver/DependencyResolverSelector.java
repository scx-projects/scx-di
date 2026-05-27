package dev.scx.di.dependency_resolver;

import dev.scx.di.dependency_point.DependencyPoint;
import dev.scx.di.exception.DependencyResolutionException;

import java.util.ArrayList;
import java.util.List;

/// DependencyResolverSelector
///
/// @author scx567888
/// @version 0.0.1
public final class DependencyResolverSelector {

    private final List<DependencyResolver> dependencyResolvers;

    public DependencyResolverSelector(List<DependencyResolver> dependencyResolvers) {
        this.dependencyResolvers = List.copyOf(dependencyResolvers);
    }

    private static DependencyResolver chooseWinnerResolver(List<DependencyResolver> candidateResolvers, List<DependencyResolver> requiredResolvers) throws DependencyResolutionException {
        if (requiredResolvers.size() == 1) {
            return requiredResolvers.get(0);
        }
        if (requiredResolvers.size() > 1) {
            throw new DependencyResolutionException("依赖解析出现歧义, 多个 REQUIRED 依赖解析器同时匹配该依赖点: " + requiredResolvers.stream().map(r -> r.getClass().getName()).toList());
        }
        if (candidateResolvers.size() == 1) {
            return candidateResolvers.get(0);
        }
        if (candidateResolvers.size() > 1) {
            throw new DependencyResolutionException("依赖解析出现歧义, 多个 CANDIDATE 依赖解析器同时匹配该依赖点: " + candidateResolvers.stream().map(r -> r.getClass().getName()).toList());
        }
        return null;
    }

    /// 找不到匹配的会返回 null
    public DependencyResolver chooseResolver(DependencyPoint dependencyPoint) {
        var candidateResolvers = new ArrayList<DependencyResolver>();
        var requiredResolvers = new ArrayList<DependencyResolver>();
        for (var dependencyResolver : dependencyResolvers) {
            var intent = dependencyResolver.match(dependencyPoint);
            switch (intent) {
                case NOT_APPLICABLE -> {
                }
                case CANDIDATE -> candidateResolvers.add(dependencyResolver);
                case REQUIRED -> requiredResolvers.add(dependencyResolver);
            }
        }

        return chooseWinnerResolver(candidateResolvers, requiredResolvers);
    }

}
