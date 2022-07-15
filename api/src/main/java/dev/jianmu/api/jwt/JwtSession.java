package dev.jianmu.api.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author huangxi
 * @class JwtSession
 * @description JwtSession
 * @create 2022-07-04 17:50
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtSession {
    /**
     * 第三方平台用户id
     */
    private String id;
    /**
     * 头像
     */
    private String avatarUrl;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 用户名
     */
    private String username;
    /**
     * git仓库id
     */
    private String gitRepoId;
    /**
     * 仓库名
     */
    private String gitRepo;
    /**
     * 仓库所有者
     */
    private String gitRepoOwner;
    /**
     * 仓库角色
     */
    private Role gitRepoRole;

    public enum Role {
        ADMIN, OWNER, MEMBER
    }
}
