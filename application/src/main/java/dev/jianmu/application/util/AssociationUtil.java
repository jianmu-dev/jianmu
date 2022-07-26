package dev.jianmu.application.util;

import dev.jianmu.oauth2.api.config.OAuth2Properties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AssociationUtil {
    private final OAuth2Properties oAuth2Properties;

    public AssociationUtil(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    public String getAssociationType() {
        if (!StringUtils.hasLength(this.oAuth2Properties.getType())) {
            return null;
        }
        return AssociationType.valueOf(this.oAuth2Properties.getType().toUpperCase()).name();
    }

    public enum AssociationType {
        /**
         * 用户
         */
        USER,
        /**
         * 组织
         */
        ORG,
        /**
         * git仓库
         */
        GIT_REPO
    }
}
