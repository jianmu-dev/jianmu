package dev.jianmu.oauth2.api.config;

import dev.jianmu.oauth2.api.exception.RoleNotExistedException;
import dev.jianmu.oauth2.api.vo.AllowLoginVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private String apiUrl;
    private String grantType;
    private String scope;
    private AllowLoginVo allowLogin;

    public void setAllowLogin(AllowLoginVo allowLogin) {
        if (allowLogin.getOrganization() == null) {
            return;
        }
        var roles = Arrays.stream(Role.values()).map(Enum::name)
                .collect(Collectors.toList());
        var noneMatch = allowLogin.getOrganization().stream()
                .noneMatch(t -> roles.contains(t.getRole()));
        if (noneMatch) {
            throw new RoleNotExistedException("oauth2.gitee.allowLogin.organization.role 配置错误，可选值：" + roles);
        }
        this.allowLogin = allowLogin;
    }

    public List<String> findRoles(String org) {
        var organization = this.getAllowLogin().getOrganization().stream()
                .filter(t -> t.getAccount().equals(org))
                .findFirst()
                .orElseThrow();
        return Role.valueOf(organization.getRole()).roles;
    }

    public enum Role {
        all(List.of("admin", "member")),
        admin(List.of("admin")),
        member(List.of("admin", "member"));

        private final List<String> roles;

        Role(List<String> roles) {
            this.roles = roles;
        }
    }
}
