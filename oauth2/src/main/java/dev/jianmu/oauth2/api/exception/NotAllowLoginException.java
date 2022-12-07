package dev.jianmu.oauth2.api.exception;

import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class NoPermissionException
 * @description NoPermissionException
 * @create 2022-07-07 17:26
 */
public class NotAllowLoginException extends BaseBusinessException{
    public NotAllowLoginException() {
        super(ErrorCodeEnum.NOT_ALLOW_LOGIN);
    }
}
