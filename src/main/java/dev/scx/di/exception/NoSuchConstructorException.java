package dev.scx.di.exception;

/// 未找到可用构造方法异常
///
/// @author scx567888
/// @version 0.0.1
public final class NoSuchConstructorException extends RuntimeException {

    public NoSuchConstructorException(String message) {
        super(message);
    }

}
