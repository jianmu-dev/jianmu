package dev.jianmu.oauth2.api.impl;

import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.NotSupportedThirdPartPlatformException;
import dev.jianmu.oauth2.api.utils.ApplicationContextUtils;
import dev.jianmu.oauth2.api.vo.IBranchesVo;
import dev.jianmu.oauth2.api.vo.IRepoMemberVo;
import dev.jianmu.oauth2.api.vo.IRepoVo;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
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
                return ApplicationContextUtils.getBean(GiteeApi.class);
            case GITLINK:
                return ApplicationContextUtils.getBean(GitlinkApi.class);
            case GITLAB:
                return ApplicationContextUtils.getBean(GitLabApi.class);
            case GITEA:
                return ApplicationContextUtils.getBean(GiteaApi.class);
            default:
                throw new NotSupportedThirdPartPlatformException();
        }
    }

    @Override
    public String getAuthUrl(String redirectUri) {
        return this.getApi().getAuthUrl(redirectUri);
    }

    @Override
    public String getAccessToken(String code, String redirectUri) {
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
    public boolean checkOrganizationMember(String accessToken, String org, String userId, String username) {
        return this.getApi().checkOrganizationMember(accessToken, org, userId, username);
    }

}
