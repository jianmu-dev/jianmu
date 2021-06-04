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

    public enum Status {
        INIT,
        RUNNING,
        FAILED,
        SUCCEEDED,
    }

    // ID
    @Schema(description = "项目ID")
    private String id;
    // 关联流程定义名称 workflowName
    @Schema(description = "项目名称")
    private String name;
    @Schema(description = "项目来源")
    private Source source;
    @Schema(description = "Git库地址")
    private String gitRepoUrl;
    @Schema(description = "最后执行时间")
    private LocalDateTime latestTime;
    @Schema(description = "下次执行时间")
    private LocalDateTime nextTime;
    @Schema(description = "最后一次执行状态")
    private Status status;
}
