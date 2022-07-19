package dev.jianmu.project.aggregate;

import java.time.LocalDateTime;

/**
 * @class ProjectLastExecution
 * @description ProjectLastExecution
 * @author Daihw
 * @create 2022/7/18 11:45 上午
 */
public class ProjectLastExecution {
    private String workflowRef;
    // 开始执行时间
    private LocalDateTime startTime;
    // 挂起时间
    private LocalDateTime suspendedTime;
    // 最后执行时间
    private LocalDateTime endTime;
    // 最后执行状态
    private String status;

    public ProjectLastExecution() {
    }

    public ProjectLastExecution(String workflowRef) {
        this.workflowRef = workflowRef;
    }

    public void running(LocalDateTime startTime, String status) {
        this.startTime = startTime;
        this.status = status;
    }

    public void end(String status, LocalDateTime endTime) {
        this.status = status;
        this.endTime = endTime;
    }

    public void suspend(String status, LocalDateTime suspendedTime) {
        this.status = status;
        this.suspendedTime = suspendedTime;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getSuspendedTime() {
        return suspendedTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }
}
