package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class ProjectGroupVo
 * @description 项目组Vo
 * @author Daihw
 * @create 2021/11/25 2:51 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "项目组Vo")
public class ProjectGroupVo {
    //ID
    private String id;
    // 名称
    private String name;
    // 描述
    private String description;
    // 排序
    private Integer sort;
    // 项目数
    private Integer projectCount;
    // 创建时间
    private LocalDateTime createdTime;
    // 最后修改时间
    private LocalDateTime lastModifiedTime;
    // 是否是默认分组
    private Boolean isDefaultGroup;
}
