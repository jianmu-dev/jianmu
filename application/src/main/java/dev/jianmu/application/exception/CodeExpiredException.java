package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class CodeExpiredException
 * @description CodeExpiredException
 * @create 2022-07-29 17:34
 */
public class CodeExpiredException extends RuntimeException{
    public CodeExpiredException(String message) {
        super(message);
    }
}
