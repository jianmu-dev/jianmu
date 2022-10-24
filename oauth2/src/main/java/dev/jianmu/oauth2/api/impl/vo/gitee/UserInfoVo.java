package dev.jianmu.oauth2.api.impl.vo.gitee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.exception.JsonParseException;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoVo implements IUserInfoVo {
    @JsonProperty("id")
    private Integer _id;

    private String login;
    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String url;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String remark;

    @JsonProperty("followers_url")
    private String followersUrl;

    @JsonProperty("following_url")
    private String followingUrl;

    @JsonProperty("gists_url")
    private String gistsUrl;

    @JsonProperty("starred_url")
    private String starredUrl;

    @JsonProperty("subscriptions_url")
    private String subscriptionsUrl;

    @JsonProperty("organizations_url")
    private String organizationsUrl;

    @JsonProperty("repos_url")
    private String reposUrl;

    @JsonProperty("events_url")
    private String eventsUrl;

    @JsonProperty("received_events_url")
    private String received_eventsUrl;

    private String type;
    private String blog;
    private String weibo;
    private String bio;

    @JsonProperty("public_repos")
    private String publicRepos;

    @JsonProperty("public_gists")
    private String publicGists;

    private String followers;
    private String following;
    private String stared;
    private String watched;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private String email;

    @Override
    @JsonIgnore
    public String getId() {
        return String.valueOf(this._id);
    }

    @Override
    public String getAvatarUrl() {
        return this.avatarUrl;
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
