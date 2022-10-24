package dev.jianmu.oauth2.api.impl.vo.gitee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GiteeTokenVo
 * @description 请求gitee的token的vo
 * @create 2021-06-30 14:08
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenVo {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    @JsonProperty("created_at")
    private long createdAt;

}