package dev.jianmu.infrastructure.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Ethan Liu
 * @class JwtProperties
 * @description JwtProperties
 * @create 2021-05-18 19:27
 */
@Data
@Component
@ConfigurationProperties(prefix = "jianmu.api")
public class JwtProperties {
    private PasswordEncoder passwordEncoder;
    private String jwtSecret;
    private int jwtExpirationMs;
    private String adminUser = "admin";
    private String adminPasswd;

    public JwtProperties() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public boolean checkAdminPassword() {
        return StringUtils.hasLength(this.adminPasswd);
    }

    public String getPassword(String password) {
        if (this.checkAdminPassword()) {
            return this.adminPasswd;
        }
        return password;
    }

    public String getEncryptedPassword(String password) {
        return this.passwordEncoder.encode(this.getPassword(password));
    }
}
