package dev.jianmu.api.oauth2_api.exception;


import dev.jianmu.api.oauth2_api.enumeration.ErrorCodeEnum;

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
