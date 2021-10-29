package dev.jianmu.application.exception;

/**
 * @class: DslException
 * @description: DslException
 * @author: Ethan Liu
 * @create: 2021-10-29 14:56
 **/
public class DslException extends RuntimeException {
    public DslException(String message) {
        super(message);
    }
}
