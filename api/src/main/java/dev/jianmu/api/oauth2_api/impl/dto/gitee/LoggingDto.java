package dev.jianmu.api.oauth2_api.impl.dto.gitee;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GiteeLoginVo
 * @description gitee登录vo
 * @create 2021-06-30 14:08
 */
@Builder
@Getter
@Setter
public class LoggingDto {
    private String grant_type;
    private String code;
    private String client_id;
    private String redirect_uri;
    private String client_secret;
}
