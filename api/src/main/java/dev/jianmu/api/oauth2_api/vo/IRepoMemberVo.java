package dev.jianmu.api.oauth2_api.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.jianmu.api.oauth2_api.impl.vo.gitlink.RepoMembersVo;

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
