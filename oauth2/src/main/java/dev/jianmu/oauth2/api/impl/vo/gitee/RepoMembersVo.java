package dev.jianmu.oauth2.api.impl.vo.gitee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jianmu.oauth2.api.vo.IRepoMemberVo;
import lombok.*;

/**
 * @author huangxi
 * @class RepoMembersVo
 * @description RepoMembersVo
 * @create 2022-07-07 15:40
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoMembersVo implements IRepoMemberVo {
    private String login;
    private String owner;
    private Permissions permissions;

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public boolean isOwner() {
        return this.login.equals(this.owner);
    }

    @Override
    public boolean isAdmin() {
        return this.permissions.isAdmin();
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Permissions {
        private boolean admin;
    }
}
