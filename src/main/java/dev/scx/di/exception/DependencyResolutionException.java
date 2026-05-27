package dev.scx.di.exception;

/// 依赖解析异常
///
/// @author scx567888
/// @version 0.0.1
public final class DependencyResolutionException extends RuntimeException {

    public DependencyResolutionException(String message) {
        super(message);
    }

}
