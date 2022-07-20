package dev.jianmu.oauth2.api;

import dev.jianmu.oauth2.api.vo.IBranchesVo;
import dev.jianmu.oauth2.api.vo.IRepoMemberVo;
import dev.jianmu.oauth2.api.vo.IRepoVo;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;

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
     String getAccessToken(String code, String redirectUri);

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

    default String getEntryUrl(String gitRepo, String gitRepoOwner){
        return null;
    }
}
