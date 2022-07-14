package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class IncorrectParameterTypeException
 * @description IncorrectParameterTypeException
 * @create 2022-07-14 10:07
 */
public class IncorrectParameterTypeException extends  RuntimeException{
    public IncorrectParameterTypeException(String message) {
        super(message);
    }
}
