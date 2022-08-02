package dev.jianmu.oauth2.api.impl;

import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.NotSupportedThirdPartPlatformException;
import dev.jianmu.oauth2.api.util.ApplicationContextUtil;
import dev.jianmu.oauth2.api.vo.*;
import lombok.Builder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author huangxi
 * @class OAuth2ApiProxy
 * @description OAuth2ApiProxy
 * @create 2021-06-30 14:08
 */
@Builder
public class OAuth2ApiProxy implements OAuth2Api {
    private String userId;
    private ThirdPartyTypeEnum thirdPartyType;

    private OAuth2Api getApi() {
        final RestTemplate restTemplate = ApplicationContextUtil.getBean(RestTemplate.class);
        final OAuth2Properties oAuth2Properties = ApplicationContextUtil.getBean(OAuth2Properties.class);
        switch (this.thirdPartyType) {
            case GITEE:
                return ApplicationContextUtil.getBean(GiteeApi.class);
            case GITLINK:
                return GitlinkApi.builder()
                        .restTemplate(restTemplate)
                        .oAuth2Properties(oAuth2Properties)
                        .userId(this.userId)
                        .build();
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

    @Override
    public IWebhookVo createWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active) {
        return this.getApi().createWebhook(accessToken, gitRepoOwner, gitRepo, url, active);
    }

    @Override
    public void deleteWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id) {
        this.getApi().deleteWebhook(accessToken, gitRepoOwner, gitRepo, id);
    }

    @Override
    public void updateWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, String id) {
        this.getApi().updateWebhook(accessToken, gitRepoOwner, gitRepo, url, active, id);
    }

    @Override
    public IWebhookVo getWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id) {
        return this.getApi().getWebhook(accessToken, gitRepoOwner, gitRepo, id);
    }

    @Override
    public void createFile(String accessToken, String owner, String repo, String content, String filepath,
                           String authorEmail, String authorName, String committerEmail, String committerName,
                           String branch, String message) {
        this.getApi().createFile(accessToken, owner, repo, content, filepath,
                authorEmail, authorName, committerEmail, committerName,
                branch, message);
    }

    @Override
    public void deleteFile(String accessToken, String owner, String repo, String content, String filepath,
                           String authorEmail, String authorName, String committerEmail, String committerName,
                           String branch, String message) {
        this.getApi().deleteFile(accessToken, owner, repo, content, filepath,
                authorEmail, authorName, committerEmail, committerName,
                branch, message);

    }

    @Override
    public void updateFile(String accessToken, String owner, String repo, String content, String filepath,
                           String authorEmail, String authorName, String committerEmail, String committerName,
                           String branch, String message) {
        this.getApi().updateFile(accessToken, owner, repo, content, filepath,
                authorEmail, authorName, committerEmail, committerName,
                branch, message);

    }

    @Override
    public IFileVo getFile(String accessToken, String owner, String repo, String filepath, String ref) {
        return this.getApi().getFile(accessToken, owner, repo, filepath, ref);
    }
}
