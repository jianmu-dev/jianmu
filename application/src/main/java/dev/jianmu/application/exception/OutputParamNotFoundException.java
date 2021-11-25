package dev.jianmu.application.exception;

/**
 * @class DslException
 * @description DslException
 * @author daihw
 * @create 2021/11/8 3:46 下午
 */
public class OutputParamNotFoundException extends RuntimeException {
    public OutputParamNotFoundException(String message) {
        super(message);
    }
}
