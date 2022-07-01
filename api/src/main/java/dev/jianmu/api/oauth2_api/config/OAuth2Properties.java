package dev.jianmu.api.oauth2_api.config;

import dev.jianmu.api.oauth2_api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.application.exception.OAuth2OnlyHaveOneException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author huangxi
 * @class OAuth2Properties
 * @description OAuth2Properties
 * @create 2022-06-30 18:21
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jianmu.oauth2")
public class OAuth2Properties implements InitializingBean {
    private boolean allowRegistration = true;
    private GiteeConfigProperties gitee;
    private GitlinkConfigProperties gitlink;
    private Environment environment;

    public String getClientSecret() {
        if (this.gitee != null) {
            return this.gitee.getClientSecret();
        }
        if (this.gitlink != null) {
            return this.gitlink.getClientSecret();
        }
        return null;
    }

    public String getThirdPartyPlatform() {
        if (gitlink != null) {
            return ThirdPartyTypeEnum.GITLINK.name();
        }
        if (gitee != null) {
            return ThirdPartyTypeEnum.GITEE.name();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int total = 0;
        if (this.gitee != null) {
            total++;
        }
        if (this.gitlink != null) {
            total++;
        }
        if (total > 1) {
            throw new OAuth2OnlyHaveOneException("oauth2不可以同时配置两个及以上");
        }
    }
}
