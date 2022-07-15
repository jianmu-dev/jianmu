package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class ExternalParameterNotFoundException
 * @description ExternalParameterNotFoundException
 * @create 2022-07-13 15:50
 */
public class ExternalParameterNotFoundException extends RuntimeException{
    public ExternalParameterNotFoundException(String message) {
        super(message);
    }
}
