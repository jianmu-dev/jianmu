package dev.jianmu.oauth2.api.exception;


import dev.jianmu.oauth2.api.enumeration.ErrorCodeEnum;

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

    public JsonParseException(String msg) {
        super(ErrorCodeEnum.JSON_PARSE_EXCEPTION, msg);
    }
}
