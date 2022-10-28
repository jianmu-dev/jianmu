package dev.jianmu.api.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author huangxi
 * @class SessionVo
 * @description SessionVo
 * @create 2022-07-04 17:50
 */

@Getter
@Setter
@ToString
public class SessionVo {
    private String clientType;
    private Long expirationTime;
    private String sessionId;
    private String accountId;
    private Boolean mobileBound;
    private String createdDate;
    private String lastModifiedDate;
    private String version;

    private String associationPlatform;
    private String associationPlatformUserId;
    private String associationId;
    private String associationType;
}
