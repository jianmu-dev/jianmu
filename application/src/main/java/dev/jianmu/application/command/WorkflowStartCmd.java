package dev.jianmu.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ethan Liu
 * @class WorkflowCmd
 * @description 流程启动命令
 * @create 2022-01-01 22:45
 */
@Data
@Builder
public class WorkflowStartCmd {
    private String triggerId;
    private String workflowRef;
    private String workflowVersion;
    private String triggerType;
}
