package dev.jianmu.workflow.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @program: workflow
 * @description: 领域事件抽象类
 * @author: Ethan Liu
 * @create: 2021-01-21 20:40
 **/
public abstract class BaseEvent implements DomainEvent {
    // 触发时间
    private LocalDateTime occurredTime;
    // 事件唯一ID
    private String identify;

    // 事件名称
    private String name = this.getClass().getSimpleName();
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
    // 任务外部ID
    protected String externalId;

    protected BaseEvent() {
        this.occurredTime = LocalDateTime.now();
        // TODO 暂时使用UUID的值
        this.identify = UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public LocalDateTime getOccurredTime() {
        return occurredTime;
    }

    @Override
    public String getIdentify() {
        return identify;
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

    public String getNodeRef() {
        return nodeRef;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "occurredTime=" + occurredTime +
                ", identify='" + identify + '\'' +
                ", name='" + name + '\'' +
                ", workflowRef='" + workflowRef + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                ", workflowInstanceId='" + workflowInstanceId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", nodeRef='" + nodeRef + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
