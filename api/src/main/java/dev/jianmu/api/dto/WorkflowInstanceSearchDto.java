package dev.jianmu.api.dto;

import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @class WorkflowInstanceSearchDto
 * @description WorkflowInstanceSearchDto
 * @author Ethan Liu
 * @create 2021-04-27 11:44
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "流程实例查询DTO")
public class WorkflowInstanceSearchDto extends PageDto {
    private String id;
    private String name;
    private String workflowVersion;
    private ProcessStatus status;
}
