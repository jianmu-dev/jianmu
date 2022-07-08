package dev.jianmu.api.oauth2_api.impl.vo.gitee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.oauth2_api.exception.JsonParseException;
import dev.jianmu.api.oauth2_api.vo.IUserInfoVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GiteeUserInfoVo
 * @description GiteeUserInfoVo
 * @create 2021-06-30 14:08
 */
@Getter
@Setter
public class UserInfoVo implements IUserInfoVo {
    @JsonProperty("id")
    private Integer _id;
    private String login;
    private String name;
    private String avatar_url;
    private String url;
    private String html_url;
    private String remark;
    private String followers_url;
    private String following_url;
    private String gists_url;
    private String starred_url;
    private String subscriptions_url;
    private String organizations_url;
    private String repos_url;
    private String events_url;
    private String received_events_url;
    private String type;
    private String blog;
    private String weibo;
    private String bio;
    private String public_repos;
    private String public_gists;
    private String followers;
    private String following;
    private String stared;
    private String watched;
    private String created_at;
    private String updated_at;
    private String email;

    @Override
    @JsonIgnore
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
        return this.login;
    }

    @Override
    public String getData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
    }
}
