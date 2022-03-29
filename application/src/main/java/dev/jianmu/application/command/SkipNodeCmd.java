package dev.jianmu.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ethan Liu
 * @class SkipNodeCmd
 * @description 跳过节点命令
 * @create 2022-01-02 10:17
 */
@Data
@Builder
public class SkipNodeCmd {
    private String triggerId;
    private String workflowRef;
    private String workflowVersion;
    private String nodeRef;
    private String sender;
}
