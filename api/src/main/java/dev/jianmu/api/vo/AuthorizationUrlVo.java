package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class AuthorizationUrlVo
 * @description 授权url的vo
 * @create 2021-06-30 14:08
 */
@Getter
@Builder
@Setter
public class AuthorizationUrlVo {
    /**
     * 授权url
     */
    @Schema(required = true)
    private String authorizationUrl;
}
