package dev.jianmu.infrastructure.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @class: RegistryProperties
 * @description: RegistryProperties
 * @author: Ethan Liu
 * @create: 2021-06-21 12:31
 **/
@Data
@Component
@ConfigurationProperties(prefix = "registry")
public class RegistryProperties {
    private String url;
    private String ak;
    private String sk;
}
