package dev.jianmu.oauth2.api.exception;


import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class AccessTokenDoesNotExistException
 * @description AccessTokenDoesNotExistException
 * @create 2021-06-30 14:08
 */
public class AccessTokenDoesNotExistException extends BaseBusinessException {
    public AccessTokenDoesNotExistException() {
        super(ErrorCodeEnum.ACCESS_TOKEN_DOES_NOT_EXIST);
    }
}
