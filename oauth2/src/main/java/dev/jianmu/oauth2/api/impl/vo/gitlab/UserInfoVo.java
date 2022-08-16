package dev.jianmu.oauth2.api.impl.vo.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class UserInfoVo
 * @description UserInfoVo
 * @create 2022-08-10 16:51
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoVo implements IUserInfoVo {
    @JsonProperty("id")
    private Integer _id;
    @JsonProperty("username")
    private String _username;
    private String name;
    private String avatar_url;

    @Override
    public String getId() {
        return String.valueOf(this._id);
    }

    @Override
    public String getAvatarUrl() {
        return this.avatar_url;
    }

    @Override
    public String getNickname() {
        return this.name;
    }

    @Override
    public String getUsername() {
        return this._username;
    }
}
