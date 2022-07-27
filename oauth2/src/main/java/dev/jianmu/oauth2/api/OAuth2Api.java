package dev.jianmu.oauth2.api;

import dev.jianmu.oauth2.api.vo.*;

import java.util.List;

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
    ITokenVo getAccessToken(String code, String redirectUri);

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @return
     */
    IUserInfoVo getUserInfo(String accessToken);

    /**
     * 获取仓库
     *
     * @param accessToken
     * @param gitRepo
     * @param gitRepoOwner
     * @return
     */
    IRepoVo getRepo(String accessToken, String gitRepo, String gitRepoOwner);

    /**
     * 获取仓库成员列表
     *
     * @param accessToken
     * @param gitRepo
     * @param gitRepoOwner
     * @return
     */
    List<? extends IRepoMemberVo> getRepoMembers(String accessToken, String gitRepo, String gitRepoOwner);

    /**
     * 获取仓库所有分支
     *
     * @param accessToken
     * @param gitRepo
     * @param gitRepoOwner
     * @return
     */
    IBranchesVo getBranches(String accessToken, String gitRepo, String gitRepoOwner);

    /**
     * 获取git仓库的流水线的准入url
     *
     * @param owner
     * @param ref
     * @return
     */
    default String getEntryUrl(String owner, String ref) {
        return null;
    }

    /**
     * 创建webhook
     *
     * @param accessToken
     * @param gitRepoOwner
     * @param gitRepo
     * @param url
     * @param active
     * @return
     */
    IWebhookVo createWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active);

    /**
     * 删除webhook
     *
     * @param accessToken
     * @param gitRepoOwner
     * @param gitRepo
     * @param id
     */
    void deleteWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id);

    /**
     * 更新webhook
     *
     * @param accessToken
     * @param gitRepoOwner
     * @param gitRepo
     * @param url
     * @param active
     * @param id
     */
    void updateWebhook(String accessToken, String gitRepoOwner, String gitRepo, String url, boolean active, String id);

    /**
     * 获取webhook
     *
     * @param accessToken
     * @param gitRepoOwner
     * @param gitRepo
     * @param id
     * @return
     */
    IWebhookVo getWebhook(String accessToken, String gitRepoOwner, String gitRepo, String id);
}
