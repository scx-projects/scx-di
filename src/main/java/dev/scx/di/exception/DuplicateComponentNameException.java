package dev.scx.di.exception;

/// 重复的 Component 名称
///
/// @author scx567888
/// @version 0.0.1
public final class DuplicateComponentNameException extends RuntimeException {

    public DuplicateComponentNameException(String message) {
        super(message);
    }

}
