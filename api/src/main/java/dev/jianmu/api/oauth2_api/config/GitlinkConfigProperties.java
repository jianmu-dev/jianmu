package dev.jianmu.api.oauth2_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * @author huangxi
 * @class GitlinkConfigProperties
 * @description GitlinkConfigProperties
 * @create 2021-06-30 14:08
 */
@Setter
@Getter
@Component
public class GitlinkConfigProperties {
    private String clientSecret;
    private String clientId;
    private String tokenUrl;
    private String userInfoUrl;
    private String grantType;
    private String baseUrl;
}
