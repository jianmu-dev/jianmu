package dev.jianmu.api.oauth2_api.exception;

import dev.jianmu.api.oauth2_api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class NoPermissionException
 * @description NoPermissionException
 * @create 2022-07-07 17:26
 */
public class NoPermissionException extends BaseBusinessException{
    public NoPermissionException(String msg) {
        super(ErrorCodeEnum.NO_PERMISSION, msg);
    }
}
