package dev.jianmu.infrastructure.credential;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @class: VaultProperties
 * @description: VaultProperties
 * @author: Ethan Liu
 * @create: 2021-11-02 09:53
 **/
@Data
@Component
@ConfigurationProperties(prefix = "credential")
public class CredentialProperties {
    private String type;
    private String url;
    private String token;
    private String path;
}
