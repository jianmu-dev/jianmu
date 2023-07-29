package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class ProjectDetailVo
 * @description ProjectDetailVo
 * @author Ethan Liu
 * @create 2021-06-28 21:25
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "项目详情VO")
public class ProjectDetailVo {
    // ID
    private String id;
    // DSL来源
    private String dslSource;
    // DSL类型
    private String dslType;
    // 触发类型
    private String triggerType;
    // Git库Id
    private String gitRepoId;
    // 关联流程定义名称
    private String workflowName;
    // 关联流程定义描述
    private String workflowDescription;
    // 关联流程定义Ref
    private String workflowRef;
    // 关联流程定义版本
    private String workflowVersion;
    // 流程节点数量
    private int steps;
    // 原始DSL文本
    private String dslText;
    // 创建时间
    private LocalDateTime createdTime;
    // 最后修改者
    private String lastModifiedBy;
    // 最后修改时间
    private LocalDateTime lastModifiedTime;
    // 项目组ID
    private String projectGroupId;
    // 项目组名称
    private String projectGroupName;
}
