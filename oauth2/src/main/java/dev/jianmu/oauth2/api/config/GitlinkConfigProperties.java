package dev.jianmu.oauth2.api.config;

import dev.jianmu.oauth2.api.exception.RoleNotExistedException;
import dev.jianmu.oauth2.api.vo.AllowLoginVo;
import lombok.Getter;
import lombok.Setter;
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
    private AllowLoginVo allowLogin;

    public void setAllowLogin(AllowLoginVo allowLogin) {
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
        this.allowLogin = allowLogin;
    }

    public enum Role {
        all,
        admin,
        developer
    }
}
