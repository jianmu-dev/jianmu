package dev.jianmu.oauth2.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * @author huangxi
 * @class GiteaConfigProperties
 * @description GiteaConfigProperties
 * @create 2021-08-24 13:08
 */
@Setter
@Getter
@Component
public class GiteaConfigProperties {
    private String clientSecret;
    private String clientId;
    private String tokenUrl;
    private String codeUrl;
    private String responseType;
    private String apiUrl;
    private String grantType;
}
