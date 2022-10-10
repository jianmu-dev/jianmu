package dev.jianmu.project.query;

import dev.jianmu.project.aggregate.Project;

import java.time.LocalDateTime;

public class ProjectVo extends Project {
    // 流程实例ID
    private String workflowInstanceId;
    // 流程实例序号
    private int serialNo;
    // 触发时间
    private LocalDateTime occurredTime;
    // 开始执行时间
    private LocalDateTime startTime;
    // 挂起时间
    private LocalDateTime suspendedTime;
    // 最后执行时间
    private LocalDateTime latestTime;
    // 最后执行状态
    private String status;

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public LocalDateTime getOccurredTime() {
        return occurredTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getSuspendedTime() {
        return suspendedTime;
    }

    public LocalDateTime getLatestTime() {
        return latestTime;
    }

    public String getStatus() {
        return status;
    }
}
