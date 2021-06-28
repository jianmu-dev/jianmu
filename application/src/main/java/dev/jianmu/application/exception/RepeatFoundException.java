package dev.jianmu.application.exception;

/**
 * @class: RepeatFoundException
 * @description: 数据重复异常
 * @author: zheng wen
 * @create: 2021-06-28 13:49
 **/
public class RepeatFoundException extends RuntimeException {
    public RepeatFoundException(String message) {
        super(message);
    }
}
