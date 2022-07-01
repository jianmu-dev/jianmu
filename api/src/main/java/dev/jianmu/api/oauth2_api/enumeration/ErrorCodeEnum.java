package dev.jianmu.api.oauth2_api.enumeration;

import lombok.Getter;

/**
 * @author huangxi
 * @class ErrorCodeEnum
 * @description 错误码枚举
 * @create 2021-06-30 14:08
 */
public enum ErrorCodeEnum {
    NOT_SUPPORTED_THIRD_PART_PLATFORM("不支持此第三方平台"),
    JSON_PARSE_EXCEPTION("json解析失败"),
    GET_TOKEN_REQUEST_PARAMETER_ERROR("请求参数错误，获取token失败"),
    ACCESS_TOKEN_DOES_NOT_EXIST("访问令牌错误或访问令牌不存在");

    @Getter
    private String message;

    ErrorCodeEnum(String message) {
        this.message = message;
    }
}
