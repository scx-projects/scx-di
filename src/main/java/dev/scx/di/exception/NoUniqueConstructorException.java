package dev.scx.di.exception;

/// 找到多个构造函数异常
///
/// @author scx567888
/// @version 0.0.1
public final class NoUniqueConstructorException extends RuntimeException {

    public NoUniqueConstructorException(String message) {
        super(message);
    }

}
