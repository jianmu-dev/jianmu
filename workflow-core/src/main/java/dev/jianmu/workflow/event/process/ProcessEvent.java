package dev.jianmu.workflow.event.process;

import dev.jianmu.workflow.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Ethan Liu
 * @class ProcessEvent
 * @description 流程运行时事件
 * @create 2022-01-01 10:32
 */
public class ProcessEvent implements DomainEvent {
    // 触发时间
    private final LocalDateTime occurredTime;
    // 事件唯一ID
    private final String identify;

    // 事件名称
    private final String name = this.getClass().getSimpleName();
    // 流程定义唯一引用名称
    protected String workflowRef;
    // 流程定义版本
    protected String workflowVersion;
    // 流程实例ID
    protected String workflowInstanceId;
    // 触发器ID
    protected String triggerId;
    // 节点唯一引用名称
    protected String nodeRef;
    // 节点类型
    protected String nodeType;

    protected ProcessEvent() {
        this.occurredTime = LocalDateTime.now();
        this.identify = UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public LocalDateTime getOccurredTime() {
        return this.occurredTime;
    }

    @Override
    public String getIdentify() {
        return this.identify;
    }

    public String getName() {
        return name;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public String getNodeType() {
        return nodeType;
    }

    @Override
    public String toString() {
        return "ProcessEvent{" +
                "occurredTime=" + occurredTime +
                ", identify='" + identify + '\'' +
                ", name='" + name + '\'' +
                ", workflowRef='" + workflowRef + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                ", workflowInstanceId='" + workflowInstanceId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", nodeRef='" + nodeRef + '\'' +
                ", nodeType='" + nodeType + '\'' +
                '}';
    }
}
