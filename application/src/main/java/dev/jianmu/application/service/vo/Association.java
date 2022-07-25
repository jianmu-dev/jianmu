package dev.jianmu.application.service.vo;

import dev.jianmu.oauth2.api.enumeration.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * @author huangxi
 * @class Association
 * @description Association
 * @create 2022-07-22 17:00
 */
@Getter
@Builder
public class Association {
    private String id;
    private String type;
    private RoleEnum role;
    private AssociationData data;
}
