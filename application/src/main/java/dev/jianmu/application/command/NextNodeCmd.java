package dev.jianmu.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ethan Liu
 * @class NextNodeCmd
 * @description 激活下游节点命令
 * @create 2022-01-02 22:14
 */
@Data
@Builder
public class NextNodeCmd {
    private String triggerId;
    private String workflowRef;
    private String workflowVersion;
    private String nodeRef;
}
