package dev.jianmu.api.oauth2_api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.oauth2_api.config.OAuth2Properties;
import dev.jianmu.api.oauth2_api.exception.JsonParseException;
import dev.jianmu.api.oauth2_api.utils.ApplicationContextUtils;
import dev.jianmu.api.oauth2_api.vo.IUserInfoVo;
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
        return ApplicationContextUtils.getBean(OAuth2Properties.class).getGitlink().getBaseUrl() + this.image_url;
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
