package dev.jianmu.api.oauth2_api.impl.vo.gitee;

import dev.jianmu.api.oauth2_api.vo.IRepoMemberVo;

/**
 * @author huangxi
 * @class RepoMembersVo
 * @description RepoMembersVo
 * @create 2022-07-07 15:40
 */
public class RepoMembersVo implements IRepoMemberVo {
    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isOwner() {
        return false;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }
}
