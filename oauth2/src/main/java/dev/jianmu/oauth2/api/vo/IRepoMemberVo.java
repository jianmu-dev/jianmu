package dev.jianmu.oauth2.api.vo;

/**
 * @author huangxi
 * @class IRepoMemberVo
 * @description IRepoMemberVo
 * @create 2022-07-07 11:47
 */
public interface IRepoMemberVo {
    String getUsername();

    boolean isOwner();

    boolean isAdmin();
}
