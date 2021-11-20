package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class: ProjectVo
 * @description: 项目VO
 * @author: Ethan Liu
 * @create: 2021-06-04 17:04
 **/
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
    @Schema(description = "项目来源")
    private Source source;
    @Schema(description = "DSL类型")
    private DslType dslType;
    @Schema(description = "Event Bridge Id", deprecated = true)
    private String eventBridgeId;
    @Schema(description = "触发类型")
    private String triggerType;
    @Schema(description = "Git库ID")
    private String gitRepoId;
    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;
    @Schema(description = "最后执行时间")
    private LocalDateTime latestTime;
    @Schema(description = "下次执行时间")
    private String nextTime;
    @Schema(description = "最后一次执行状态")
    private String status;
}
