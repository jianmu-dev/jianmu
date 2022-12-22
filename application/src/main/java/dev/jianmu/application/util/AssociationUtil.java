package dev.jianmu.application.util;

import dev.jianmu.application.exception.NoAssociatedPermissionException;
import dev.jianmu.jianmu_user_context.exception.NotOrganizationMemberException;
import dev.jianmu.jianmu_user_context.exception.UserIdIllegalException;
import dev.jianmu.jianmu_user_context.exception.UserRefIllegalException;
import dev.jianmu.jianmu_user_context.holder.UserPermissionHolder;
import dev.jianmu.project.aggregate.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Daihw
 * @class AssociationUtil
 * @description AssociationUtil
 * @create 2022/12/15 9:51 上午
 */
@Slf4j
@Component
public class AssociationUtil {
    @Resource
    private UserPermissionHolder userPermissionHolder;

    // 校验项目查看权限
    public void checkProjectViewPermission(String associationId, String associationType, String associationPlatform, Project project) {
        if (project.getAssociationId().isBlank() || project.getAssociationType().isBlank()) {
            return;
        }
        var type = AssociationUtil.AssociationType.valueOf(project.getAssociationType());
        switch (type) {
            case GIT_REPO:
                if (!associationId.equals(project.getAssociationId()) || !associationType.equals(project.getAssociationType()) || !associationPlatform.equals(project.getAssociationPlatform())) {
                    throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
                }
                break;
            case PERSONAL:
                try {
                    this.userPermissionHolder.getPermission().checkUserRef(project.getAssociationId());
                } catch (UserRefIllegalException e) {
                    throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
                }
                break;
            case ORGANIZATION:
                var permission = this.userPermissionHolder.getPermission();
                try {
                    permission.checkOrganizationMember(project.getAssociationId());
                } catch (NotOrganizationMemberException e) {
                    throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
                }
                break;
            default:
                log.warn("未知联合类型: {}", type);
        }
    }

    // 校验项目操作权限
    public void checkProjectPermission(String associationId, String associationType, String associationPlatform, Project project) {
        if (project.getAssociationId().isBlank() || project.getAssociationType().isBlank()) {
            return;
        }
        var type = AssociationUtil.AssociationType.valueOf(project.getAssociationType());
        switch (type) {
            case GIT_REPO:
                if (!associationId.equals(project.getAssociationId()) || !associationType.equals(project.getAssociationType()) || !associationPlatform.equals(project.getAssociationPlatform())) {
                    throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
                }
                break;
            case PERSONAL:
                try {
                    this.userPermissionHolder.getPermission().checkUserRef(project.getAssociationId());
                } catch (UserRefIllegalException e) {
                    throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
                }
                break;
            case ORGANIZATION:
                var permission = this.userPermissionHolder.getPermission();
                try {
                    permission.checkUserId(project.getCreatorId());
                } catch (UserIdIllegalException e) {
                    try {
                        this.userPermissionHolder.getPermission().checkOrganizationMember(project.getAssociationId(), "ADMIN");
                    } catch (NotOrganizationMemberException exception) {
                        throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
                    }
                }
                break;
            default:
                log.warn("未知联合类型: {}", type);
        }
    }

    public enum AssociationType {
        /**
         * 用户
         */
        PERSONAL,
        /**
         * 组织
         */
        ORGANIZATION,
        /**
         * git仓库
         */
        GIT_REPO
    }

    public enum AssociationPlatform {
        GITLINK,
        AUTO_BUILD_IMAGE,
    }
}
