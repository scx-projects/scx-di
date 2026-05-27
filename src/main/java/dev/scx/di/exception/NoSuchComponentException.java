package dev.scx.di.exception;

/// 未找到 对应的 Component
///
/// @author scx567888
/// @version 0.0.1
public final class NoSuchComponentException extends RuntimeException {

    public NoSuchComponentException(String message) {
        super(message);
    }

}
