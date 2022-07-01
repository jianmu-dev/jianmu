package dev.jianmu.api.oauth2_api.impl;

import dev.jianmu.api.oauth2_api.OAuth2Api;
import dev.jianmu.api.oauth2_api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.api.oauth2_api.exception.NotSupportedThirdPartPlatformException;
import dev.jianmu.api.oauth2_api.utils.ApplicationContextUtils;
import dev.jianmu.api.oauth2_api.vo.UserInfoVo;
import lombok.Builder;

/**
 * @author huangxi
 * @class OAuth2ApiProxy
 * @description OAuth2ApiProxy
 * @create 2021-06-30 14:08
 */
@Builder
public class OAuth2ApiProxy implements OAuth2Api {
    private ThirdPartyTypeEnum thirdPartyType;

    private OAuth2Api getApi() {
        switch (this.thirdPartyType){
            case GITEE:
                return ApplicationContextUtils.getBean(GiteeApi.class);
            case GITLINK:
                return ApplicationContextUtils.getBean(GitlinkApi.class);
            default:
                throw new NotSupportedThirdPartPlatformException();
        }
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        OAuth2Api api = getApi();
        return api.getAuthUrl(redirectUri);
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
        OAuth2Api api = getApi();
        return api.getAccessToken(code, redirectUri);
    }

    @Override
    public UserInfoVo getUserInfoVo(String token) {
        OAuth2Api api = getApi();
        return api.getUserInfoVo(token);
    }
}
