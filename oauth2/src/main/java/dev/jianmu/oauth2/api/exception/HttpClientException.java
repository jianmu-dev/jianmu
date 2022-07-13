package dev.jianmu.oauth2.api.exception;

import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class HttpClientErrorException
 * @description HttpClientErrorException
 * @create 2022-07-06 15:41
 */
public class HttpClientException extends BaseBusinessException {
    public HttpClientException() {
        super(ErrorCodeEnum.HTTP_CLIENT_ERROR);
    }

    public HttpClientException(String msg) {
        super(ErrorCodeEnum.HTTP_CLIENT_ERROR, msg);
    }
}
