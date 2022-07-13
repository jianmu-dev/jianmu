package dev.jianmu.oauth2.api.vo;

/**
 * @author huangxi
 * @class IRepoVo
 * @description IRepoVo
 * @create 2022-07-07 11:47
 */
public interface IRepoVo {
    String getId();

    String getOwner();

    String getRepo();

    String getDefaultBranch();
}
