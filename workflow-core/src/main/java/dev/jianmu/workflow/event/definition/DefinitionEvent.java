package dev.jianmu.workflow.event.definition;

import dev.jianmu.workflow.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Ethan Liu
 * @class DefinitionEvent
 * @description 流程定义事件
 * @create 2022-01-01 10:35
 */
public class DefinitionEvent implements DomainEvent {
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
    // 触发器ID
    protected String triggerId;
    // 节点唯一引用名称
    protected String nodeRef;
    // 节点类型
    protected String nodeType;
    // 事件发送者
    protected String sender;

    public DefinitionEvent() {
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

    public String getTriggerId() {
        return triggerId;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "DefinitionEvent{" +
                "occurredTime=" + occurredTime +
                ", identify='" + identify + '\'' +
                ", name='" + name + '\'' +
                ", workflowRef='" + workflowRef + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", nodeRef='" + nodeRef + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
