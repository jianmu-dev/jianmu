package dev.jianmu.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ethan Liu
 * @class ActivateNodeCmd
 * @description 节点激活命令
 * @create 2022-01-02 10:10
 */
@Data
@Builder
public class ActivateNodeCmd {
    private String triggerId;
    private String workflowRef;
    private String workflowVersion;
    private String nodeRef;
}
