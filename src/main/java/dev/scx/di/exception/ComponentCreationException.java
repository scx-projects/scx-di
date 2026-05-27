package dev.scx.di.exception;

/// Component 创建异常
///
/// @author scx567888
/// @version 0.0.1
public final class ComponentCreationException extends RuntimeException {

    public ComponentCreationException(String message) {
        super(message);
    }

    public ComponentCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
