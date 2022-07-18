package dev.jianmu.oauth2.api.exception;

import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class UnKnownException
 * @description UnKnownException
 * @create 2022-07-08 10:55
 */
public class UnKnownException extends BaseBusinessException{
    public UnKnownException(String msg) {
        super(ErrorCodeEnum.UNKNOWN_ERROR, msg);
    }
}
