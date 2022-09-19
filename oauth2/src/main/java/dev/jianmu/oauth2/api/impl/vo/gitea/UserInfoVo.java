package dev.jianmu.oauth2.api.impl.vo.gitea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class UserInfoVo
 * @description UserInfoVo
 * @create 2022-08-24 15:01
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoVo implements IUserInfoVo {
    @JsonProperty("id")
    private Integer _id;
    @JsonProperty("username")
    private String _username;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("avatar_url")
    private String avatarUrl;

    @Override
    public String getId() {
        return String.valueOf(this._id);
    }

    @Override
    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Override
    public String getNickname() {
        return this.fullName;
    }

    @Override
    public String getUsername() {
        return this._username;
    }
}
