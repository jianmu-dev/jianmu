package dev.jianmu.oauth2.api.config;

import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.OAuth2MustHaveOneException;
import dev.jianmu.oauth2.api.exception.OAuth2OnlyHaveOneException;
import dev.jianmu.oauth2.api.vo.AllowLoginVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * @author huangxi
 * @class OAuth2Properties
 * @description OAuth2Properties
 * @create 2022-06-30 18:21
 */
@Setter
@Getter
@Component
@Validated
@ConfigurationProperties(prefix = "jianmu.oauth2")
public class OAuth2Properties implements InitializingBean, EnvironmentAware {
    private boolean entry = false;
    private boolean allowRegistration = true;
    private GiteeConfigProperties gitee;
    private GitlinkConfigProperties gitlink;
    private GitLabConfigProperties gitlab;
    private GiteaConfigProperties gitea;
    private Environment environment;

    public String getClientSecret() {
        if (this.gitee != null) {
            return this.gitee.getClientSecret();
        }
        if (this.gitlink != null) {
            return this.gitlink.getClientSecret();
        }
        if (this.gitlab != null) {
            return this.gitlab.getClientSecret();
        }
        if (this.gitea != null) {
            return this.gitea.getClientSecret();
        }
        return null;
    }

    public AllowLoginVo getAllowLogin() {
        if (this.gitee != null) {
            return this.gitee.getAllowLogin();
        }
        if (this.gitlink != null) {
            return this.gitlink.getAllowLogin();
        }
        if (this.gitlab != null) {
            return this.gitlab.getAllowLogin();
        }
        if (this.gitea != null) {
            return this.gitea.getAllowLogin();
        }
        return null;
    }

    public String getThirdPartyType() {
        if (gitlink != null) {
            return ThirdPartyTypeEnum.GITLINK.name();
        }
        if (gitee != null) {
            return ThirdPartyTypeEnum.GITEE.name();
        }
        if (gitlab != null) {
            return ThirdPartyTypeEnum.GITLAB.name();
        }
        if (gitea != null) {
            return ThirdPartyTypeEnum.GITEA.name();
        }
        return "";
    }

    @Override
    public void afterPropertiesSet() {
        // 当配置有OAuth2时，必须存在一个
        if (Objects.equals(environment.getProperty("jianmu.oauth2"), "")) {
            throw new OAuth2MustHaveOneException("oauth2配置必须存在一个");
        }

        // oauth2不可以同时配置两个及以上
        int total = 0;
        if (this.gitee != null) {
            total++;
        }
        if (this.gitlink != null) {
            total++;
        }
        if (this.gitlab != null) {
            total++;
        }
        if (this.gitea != null) {
            total++;
        }
        if (total > 1) {
            throw new OAuth2OnlyHaveOneException("oauth2不可以同时配置两个及以上");
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
