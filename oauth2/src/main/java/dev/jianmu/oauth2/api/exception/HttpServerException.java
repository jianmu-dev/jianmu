package dev.jianmu.oauth2.api.exception;

import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class HttpServerErrorException
 * @description HttpServerErrorException
 * @create 2022-07-06 15:41
 */
public class HttpServerException extends BaseBusinessException {
    public HttpServerException() {
        super(ErrorCodeEnum.HTTP_SERVER_ERROR);
    }

    public HttpServerException(String msg) {
        super(ErrorCodeEnum.HTTP_SERVER_ERROR, msg);
    }
}
