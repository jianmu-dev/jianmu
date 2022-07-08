package dev.jianmu.api.oauth2_api.exception;

import dev.jianmu.api.oauth2_api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class HttpClientErrorException
 * @description HttpClientErrorException
 * @create 2022-07-06 15:41
 */
public class HttpClientException extends BaseBusinessException{
    public HttpClientException() {
        super(ErrorCodeEnum.HTTP_CLIENT_ERROR);
    }
}
