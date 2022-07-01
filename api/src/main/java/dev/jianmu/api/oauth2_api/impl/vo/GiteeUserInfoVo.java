package dev.jianmu.api.oauth2_api.impl.vo;

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
public class GiteeUserInfoVo {
    private Integer id;
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
}
