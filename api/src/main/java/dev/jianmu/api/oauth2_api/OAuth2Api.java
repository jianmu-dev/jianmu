package dev.jianmu.api.oauth2_api;

import dev.jianmu.api.oauth2_api.vo.UserInfoVo;

/**
 * @author huangxi
 * @class OAuth2Api
 * @description OAuth2Api
 * @create 2021-06-30 14:08
 */
public interface OAuth2Api {
    /**
     * 获取授权地址
     *
     * @param redirectUri
     * @return
     */
    String getAuthUrl(String redirectUri);

    /**
     * 获取accessToken
     *
     * @param code
     * @return
     */
    default String getAccessToken(String code) {
        return this.getAccessToken(code, null);
    }

    /**
     * 获取accessToken
     *
     * @param code
     * @return
     */
    default String getAccessToken(String code, String redirectUri) {
        return null;
    }


    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    UserInfoVo getUserInfoVo(String token);

}
