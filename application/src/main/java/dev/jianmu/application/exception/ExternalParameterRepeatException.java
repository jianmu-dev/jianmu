package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class ExternalParameterRepeatException
 * @description ExternalParameterRepeatException
 * @create 2022-07-14 22:07
 */
public class ExternalParameterRepeatException extends RuntimeException {
    public ExternalParameterRepeatException(String message) {
        super(message);
    }
}
