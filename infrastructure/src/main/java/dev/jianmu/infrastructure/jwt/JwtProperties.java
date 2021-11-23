package dev.jianmu.infrastructure.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @class JwtProperties
 * @description JwtProperties
 * @author Ethan Liu
 * @create 2021-05-18 19:27
*/
@Data
@Component
@ConfigurationProperties(prefix = "jianmu.api")
public class JwtProperties {
    private String jwtSecret;
    private int jwtExpirationMs;
    private String adminUser = "admin";
    private String adminPasswd;
}
