package dev.jianmu.task.event;

import dev.jianmu.task.aggregate.Volume;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class VolumeCreatedEvent
 * @description VolumeCreatedEvent
 * @author Daihw
 * @create 2023/3/1 5:12 下午
 */
public class VolumeCreatedEvent implements DomainEvent {
    // 触发时间
    private final LocalDateTime occurredTime = LocalDateTime.now();
    // 事件唯一ID
    private final String identify = UUID.randomUUID().toString().replace("-", "");

    // 名称
    private String name;
    // 作用域
    private Volume.Scope scope;
    // 关联项目ref
    private String workflowRef;

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

    public Volume.Scope getScope() {
        return scope;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public static Builder aVolumeCreatedEvent() {
        return new Builder();
    }

    public static class Builder{
        private String name;
        private Volume.Scope scope;
        private String workflowRef;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder scope(Volume.Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public VolumeCreatedEvent build() {
            var event = new VolumeCreatedEvent();
            event.name = this.name;
            event.scope = this.scope;
            event.workflowRef = this.workflowRef;
            return event;
        }
    }
}
