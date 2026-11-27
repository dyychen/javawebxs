package com.example.canteen.common;

/**
 * 当前登录用户无权访问该资源。
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
