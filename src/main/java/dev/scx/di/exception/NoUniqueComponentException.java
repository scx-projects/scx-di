package dev.scx.di.exception;

/// 不是唯一符合条件的 Component
///
/// @author scx567888
/// @version 0.0.1
public final class NoUniqueComponentException extends RuntimeException {

    public NoUniqueComponentException(String message) {
        super(message);
    }

}
