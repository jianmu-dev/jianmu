package dev.jianmu.oauth2.api.impl.vo.gitea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class TokenVo
 * @description TokenVo
 * @create 2022-08-24 15:01
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
}
