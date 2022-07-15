package dev.jianmu.oauth2.api.exception;

/**
 * @author huangxi
 * @class OAuth2ConfigurationCanOnlyHaveOneException
 * @description OAuth2ConfigurationCanOnlyHaveOneException
 * @create 2022-07-01 11:04
 */
public class OAuth2OnlyHaveOneException extends RuntimeException{
    public OAuth2OnlyHaveOneException(String message) {
        super(message);
    }
}
