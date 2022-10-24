package dev.jianmu.oauth2.api.impl.vo.gitlink;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GitlinkTokenVo
 * @description 请求gitlink的token的vo
 * @create 2021-07-05 18:11
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