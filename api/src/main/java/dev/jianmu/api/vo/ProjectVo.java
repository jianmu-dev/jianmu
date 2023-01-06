package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Ethan Liu
 * @class ProjectVo
 * @description 项目VO
 * @create 2021-06-04 17:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "项目VO")
public class ProjectVo {
    public enum Source {
        GIT,
        LOCAL
    }

    public enum DslType {
        WORKFLOW,
        PIPELINE
    }

    // ID
    @Schema(description = "项目ID")
    private String id;
    // 关联流程定义名称 workflowName
    @Schema(description = "项目名称")
    private String name;
    @Schema(description = "项目描述")
    private String description;
    @Schema(description = "项目来源")
    private Source source;
    @Schema(description = "DSL类型")
    private DslType dslType;
    @Schema(description = "触发类型")
    private String triggerType;
    @Schema(description = "项目状态")
    private boolean enabled;
    @Schema(description = "状态是否可变")
    private boolean mutable;
    @Schema(description = "可否并发执行")
    private int concurrent;
    @Schema(description = "Git库ID")
    private String gitRepoId;
    @Schema(description = "流程实例ID")
    private String workflowInstanceId;
    @Schema(description = "流程实例序号")
    private int serialNo;
    @Schema(description = "触发时间")
    private LocalDateTime occurredTime;
    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;
    @Schema(description = "挂起时间")
    private LocalDateTime suspendedTime;
    @Schema(description = "最后执行时间")
    private LocalDateTime latestTime;
    @Schema(description = "下次执行时间")
    private LocalDateTime nextTime;
    @Schema(description = "最后修改时间")
    private LocalDateTime lastModifiedTime;
    @Schema(description = "最后一次执行状态")
    private String status;
    @Schema(description = "分支")
    private String branch;
}
