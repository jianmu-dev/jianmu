package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class OAuth2MustHaveOneException
 * @description OAuth2MustHaveOneException
 * @create 2022-07-01 18:01
 */
public class OAuth2MustHaveOneException extends RuntimeException{
    public OAuth2MustHaveOneException(String message) {
        super(message);
    }
}
