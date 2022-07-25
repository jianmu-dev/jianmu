package dev.jianmu.oauth2.api.vo;

/**
 * @author huangxi
 * @class ITokenVo
 * @description ITokenVo
 * @create 2022-07-19 13:59
 */
public interface ITokenVo {
    String getAccessToken();

    long getExpireInMs();

    String getEncryptedAccessToken();
}
