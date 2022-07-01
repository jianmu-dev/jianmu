package dev.jianmu.api.oauth2_api.impl.vo;

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
public class GitlinkUserInfoVo {
    private String username;
    private String login;
    private String image_url;
    private boolean admin;
    private long user_id;
}
