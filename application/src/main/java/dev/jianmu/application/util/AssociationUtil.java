package dev.jianmu.application.util;

import org.springframework.stereotype.Component;

@Component
public class AssociationUtil {
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
