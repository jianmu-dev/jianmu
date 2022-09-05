package dev.jianmu.application.exception;

/**
 * @class IgnoreSyncEventException
 * @description 忽略同步流水线DSL事件异常
 * @author Daihw
 * @create 2022/9/5 2:39 下午
 */
public class IgnoreSyncDslEventException extends RuntimeException{
    public IgnoreSyncDslEventException(String message) {
        super(message);
    }
}
