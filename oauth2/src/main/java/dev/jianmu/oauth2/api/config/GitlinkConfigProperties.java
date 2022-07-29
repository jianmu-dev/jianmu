package dev.jianmu.oauth2.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    private String apiUrl;
    private String grantType;
    private String baseUrl;
    private String username;
    private String password;
    private String responseType;
    private SilentLogin silentLogin;

    @Setter
    @Getter
    @Component
    @ConfigurationProperties(prefix = "jianmu.oauth2.gitlink.silent-login")
    public static class SilentLogin{
        private String iv;
        private String key;
        private int codeTimeout;
    }
}
