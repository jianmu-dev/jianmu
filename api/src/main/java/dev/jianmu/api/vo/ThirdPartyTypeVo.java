package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class ThirdPartyPlatformVo
 * @description ThirdPartyPlatformVo
 * @create 2022-07-01 10:53
 */
@Getter
@Builder
@Setter
public class ThirdPartyTypeVo {
    /**
     * 第三方平台
     */
    @Schema(required = true)
    private String thirdPartyType;

    /**
     * gitRepoEntry
     */
    @Schema(required = true)
    private boolean entry;

    /**
     * authMode
     */
    @Schema(required = true)
    private String authMode;
}