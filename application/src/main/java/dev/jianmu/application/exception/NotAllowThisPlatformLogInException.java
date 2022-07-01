package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class NotAllowThisPlatformLogInException
 * @description NotAllowThisPlatformLogInException
 * @create 2022-06-30 17:40
 */
public class NotAllowThisPlatformLogInException extends RuntimeException {
    public NotAllowThisPlatformLogInException(String message) {
        super(message);
    }
}
