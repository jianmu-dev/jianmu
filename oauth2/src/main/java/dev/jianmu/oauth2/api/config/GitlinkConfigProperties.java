package dev.jianmu.oauth2.api.config;

import dev.jianmu.oauth2.api.exception.RoleNotExistedException;
import dev.jianmu.oauth2.api.vo.AllowLoginVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    private String engineAddress;
    private AllowLoginVo allowLogin;

    @Setter
    @Getter
    @Component
    @ConfigurationProperties(prefix = "jianmu.oauth2.gitlink.silent-login")
    public static class SilentLogin{
        private String iv;
        private String key;
        private int codeTimeout;
    }

    public void setAllowLogin(AllowLoginVo allowLogin) {
        this.allowLogin = allowLogin;
        if (allowLogin.getOrganization() == null) {
            return;
        }
        var roles = Arrays.stream(GiteeConfigProperties.Role.values()).map(Enum::name)
                .collect(Collectors.toList());
        var noneMatch = allowLogin.getOrganization().stream()
                .noneMatch(t -> roles.contains(t.getRole()));
        if (noneMatch) {
            throw new RoleNotExistedException("oauth2.gitlink.allowLogin.organization.role 配置错误，可选值：" + roles);
        }
    }

    public enum Role {
        all,
        admin,
        developer
    }
}
