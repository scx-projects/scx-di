package dev.scx.di.exception;

/// 非法的 Component 类型 比如说 接口
///
/// @author scx567888
/// @version 0.0.1
public final class IllegalComponentTypeException extends RuntimeException {

    public IllegalComponentTypeException(String message) {
        super(message);
    }

}
