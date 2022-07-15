package dev.jianmu.api.util;

import dev.jianmu.oauth2.api.config.OAuth2Properties;
import org.springframework.stereotype.Component;

@Component
public class AssociationUtil {
    private final OAuth2Properties oAuth2Properties;

    public AssociationUtil(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    public String getAssociationType() {
        if (this.oAuth2Properties.isEntry()) {
            return AssociationType.GIT_REPO.name();
        }
        return null;
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
