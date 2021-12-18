package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * @author laoji
 * @class ProjectIdVo
 * @description 项目id值对象
 * @create 2021-12-18 16:08
 */
@Getter
@Builder
public class ProjectIdVo {
    @Schema(description = "项目ID")
    private final String id;
}
