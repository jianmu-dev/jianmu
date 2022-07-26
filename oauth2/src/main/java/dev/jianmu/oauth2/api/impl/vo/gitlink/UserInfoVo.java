package dev.jianmu.oauth2.api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.util.ApplicationContextUtil;
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
    private String image_url;
    private boolean admin;
    private long user_id;

    @Override
    public String getId() {
        return String.valueOf(this.user_id);
    }

    @Override
    public String getAvatarUrl() {
        return ApplicationContextUtil.getBean(OAuth2Properties.class).getGitlink().getBaseUrl() + this.image_url;
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
