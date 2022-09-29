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
    // 流程实例ID
    private String workflowInstanceId;
    // 流程实例序号
    private int serialNo;
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

    public void running(String workflowInstanceId, int serialNo, LocalDateTime startTime, String status) {
        this.workflowInstanceId = workflowInstanceId;
        this.serialNo = serialNo;
        this.startTime = startTime;
        this.status = status;
    }

    public void end(String workflowInstanceId, int serialNo, String status, LocalDateTime endTime) {
        this.workflowInstanceId = workflowInstanceId;
        this.serialNo = serialNo;
        this.status = status;
        this.endTime = endTime;
    }

    public void suspend(String workflowInstanceId, int serialNo, String status, LocalDateTime suspendedTime) {
        this.workflowInstanceId = workflowInstanceId;
        this.serialNo = serialNo;
        this.status = status;
        this.suspendedTime = suspendedTime;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }


    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public int getSerialNo() {
        return serialNo;
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
