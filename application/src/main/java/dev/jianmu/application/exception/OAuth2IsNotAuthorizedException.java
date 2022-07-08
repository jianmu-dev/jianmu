package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class Oauth2IsNotAuthorizedException
 * @description Oauth2IsNotAuthorizedException
 * @create 2022-07-06 17:08
 */
public class OAuth2IsNotAuthorizedException extends RuntimeException{
    public OAuth2IsNotAuthorizedException(String message) {
        super(message);
    }
}
