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
 * @class GitLabConfigProperties
 * @description GitLabConfigProperties
 * @create 2021-08-10 15:08
 */
@Setter
@Getter
@Component
public class GitLabConfigProperties {
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
        this.allowLogin = allowLogin;
        if (allowLogin.getOrganization() == null) {
            return;
        }
        var roles = Arrays.stream(GitLabConfigProperties.Role.values()).map(Enum::name)
                .collect(Collectors.toList());
        var noneMatch = allowLogin.getOrganization().stream()
                .noneMatch(t -> roles.contains(t.getRole()));
        if (noneMatch) {
            throw new RoleNotExistedException("oauth2.gitlab.allowLogin.organization.role 配置错误，可选值：" + roles);
        }
    }

    public Integer findAccessLevel(String org) {
        var organization = this.getAllowLogin().getOrganization().stream()
                .filter(t -> t.getAccount().equals(org))
                .findFirst()
                .orElseThrow();
        return GitLabConfigProperties.Role.valueOf(organization.getRole()).accessLevel;
    }

    public enum Role {
        all(10),
        Guest(10),
        Reporter(20),
        Developer(30),
        Maintainer(40),
        Owner(50);

        private final Integer accessLevel;

        Role(Integer accessLevel) {
            this.accessLevel = accessLevel;
        }
    }
}
