package dev.jianmu.api.oauth2_api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.api.oauth2_api.vo.IRepoMemberVo;
import lombok.*;

import java.util.List;

/**
 * @author huangxi
 * @class GitlinkRepoMemberVo
 * @description GitlinkRepoMemberVo
 * @create 2022-07-06 18:03
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoMembersVo {
    @JsonProperty("total_count")
    private int totalCount;
    private List<MemberVo> members;
    private Integer status;
    private String message;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberVo implements IRepoMemberVo {
        private int id;
        private String name;
        private String login;
        @JsonProperty("image_url")
        private String imageUrl;
        private String email;
        @JsonProperty("is_owner")
        private boolean isOwner;
        private String role;
        @JsonProperty("role_name")
        private String roleName;

        @Override
        public String getUsername() {
            return this.login;
        }

        @Override
        public boolean isAdmin() {
            return this.role.equals("Manager");
        }
    }
}