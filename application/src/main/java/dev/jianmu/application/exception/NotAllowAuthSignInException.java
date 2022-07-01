package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class NotAllowRegistrationException
 * @description NotAllowRegistrationException
 * @create 2022-06-30 17:06
 */
public class NotAllowAuthSignInException extends RuntimeException{
    public NotAllowAuthSignInException(String message) {
        super(message);
    }
}
