package dev.jianmu.oauth2.api.impl.vo.gitlab;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class TokenVo
 * @description 请求gitlab的token的vo
 * @create 2021-08-10 16:08
 */
@Getter
@Setter
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