package dev.jianmu.api.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSessionVo {
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
