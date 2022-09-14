package dev.jianmu.oauth2.api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.utils.ApplicationContextUtils;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GitlinkUserInfoVo
 * @description GitlinkUserInfoVo
 * @create 2021-06-30 14:08
 */
@Getter
@Setter
public class UserInfoVo implements IUserInfoVo {
    @JsonProperty("username")
    private String username_;

    private String login;

    @JsonProperty("image_url")
    private String imageUrl;

    private boolean admin;

    @JsonProperty("user_id")
    private long userId;

    @Override
    public String getId() {
        return String.valueOf(this.userId);
    }

    @Override
    public String getAvatarUrl() {
        return ApplicationContextUtils.getBean(OAuth2Properties.class).getGitlink().getBaseUrl() + this.imageUrl;
    }

    @Override
    public String getNickname() {
        return this.username_;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.login;
    }
}
