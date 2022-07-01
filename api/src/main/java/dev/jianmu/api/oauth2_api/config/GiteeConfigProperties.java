package dev.jianmu.api.oauth2_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * @author huangxi
 * @class GiteeConfigProperties
 * @description GiteeConfigProperties
 * @create 2021-06-30 14:08
 */
@Setter
@Getter
@Component
public class GiteeConfigProperties {
    private String clientSecret;
    private String clientId;
    private String tokenUrl;
    private String codeUrl;
    private String responseType;
    private String userInfoUrl;
    private String grantType;
    private String scope;
}
