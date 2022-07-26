package dev.jianmu.oauth2.api.impl;

import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.NotSupportedThirdPartPlatformException;
import dev.jianmu.oauth2.api.util.ApplicationContextUtil;
import dev.jianmu.oauth2.api.vo.*;
import lombok.Builder;

import java.util.List;

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
        switch (this.thirdPartyType) {
            case GITEE:
                return ApplicationContextUtil.getBean(GiteeApi.class);
            case GITLINK:
                return ApplicationContextUtil.getBean(GitlinkApi.class);
            default:
                throw new NotSupportedThirdPartPlatformException();
        }
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        return this.getApi().getAuthUrl(redirectUri);
    }

    @Override
    public ITokenVo getAccessToken(String code, String redirectUri) {
        return this.getApi().getAccessToken(code, redirectUri);
    }

    @Override
    public IUserInfoVo getUserInfo(String token) {
        return this.getApi().getUserInfo(token);
    }

    @Override
    public IRepoVo getRepo(String accessToken, String gitRepo, String gitRepoOwner) {
        return this.getApi().getRepo(accessToken, gitRepo, gitRepoOwner);
    }

    @Override
    public List<? extends IRepoMemberVo> getRepoMembers(String accessToken, String gitRepo, String gitRepoOwner) {
        return this.getApi().getRepoMembers(accessToken, gitRepo, gitRepoOwner);
    }

    @Override
    public IBranchesVo getBranches(String accessToken, String gitRepo, String gitRepoOwner) {
        return this.getApi().getBranches(accessToken, gitRepo, gitRepoOwner);
    }

    @Override
    public String getEntryUrl(String owner, String ref) {
        return this.getApi().getEntryUrl(owner, ref);
    }
}
