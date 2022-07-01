package dev.jianmu.api.oauth2_api.exception;


import dev.jianmu.api.oauth2_api.enumeration.ErrorCodeEnum;

/**
 * @author huangxi
 * @class JsonParseException
 * @description JsonParseException
 * @create 2021-06-30 14:08
 */
public class JsonParseException extends BaseBusinessException {
    public JsonParseException() {
        super(ErrorCodeEnum.JSON_PARSE_EXCEPTION);
    }
}
