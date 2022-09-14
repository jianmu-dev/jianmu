package dev.jianmu.oauth2.api.impl.dto.gitlink;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class LoggingDto {
    @JsonProperty("grant_type")
    private String grantType;

    private String code;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("client_secret")
    private String clientSecret;
}
