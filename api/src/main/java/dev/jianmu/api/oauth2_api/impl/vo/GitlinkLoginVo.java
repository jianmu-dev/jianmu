package dev.jianmu.api.oauth2_api.impl.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GitlinkLoginVo
 * @description gitee登录vo
 * @create 2021-07-05 17:37
 */
@Builder
@Getter
@Setter
public class GitlinkLoginVo {
    private String grant_type;
    private String code;
    private String client_id;
    private String redirect_uri;
    private String client_secret;
}
