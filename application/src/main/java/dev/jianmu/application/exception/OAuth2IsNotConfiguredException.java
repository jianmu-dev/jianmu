package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class ClientIdNotConfigureException
 * @description ClientIdNotConfigureException
 * @create 2022-06-30 16:57
 */
public class OAuth2IsNotConfiguredException extends RuntimeException {
    public OAuth2IsNotConfiguredException(String message) {
        super(message);
    }
}

