package dev.jianmu.oauth2.api.exception;


import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class GetTokenRequestParameterErrorException
 * @description GetTokenRequestParameterErrorException
 * @create 2021-06-30 14:08
 */
public class GetTokenRequestParameterErrorException extends BaseBusinessException {
    public GetTokenRequestParameterErrorException() {
        super(ErrorCodeEnum.GET_TOKEN_REQUEST_PARAMETER_ERROR);
    }
}
