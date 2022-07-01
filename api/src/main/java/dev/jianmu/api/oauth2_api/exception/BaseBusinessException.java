package dev.jianmu.api.oauth2_api.exception;

import dev.jianmu.api.oauth2_api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class BaseBusinessException
 * @description 基础异常类
 * @create 2021-06-30 14:08
 */
public abstract class BaseBusinessException extends RuntimeException {

    private ErrorCodeEnum errorCode;

    protected BaseBusinessException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
