package dev.jianmu.task.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Daihw
 * @class VolumeDeletedEvent
 * @description VolumeDeletedEvent
 * @create 2023/3/1 5:12 下午
 */
public class VolumeDeletedEvent implements DomainEvent {
    // 触发时间
    private final LocalDateTime occurredTime = LocalDateTime.now();
    // 事件唯一ID
    private final String identify = UUID.randomUUID().toString().replace("-", "");

    // volumeId
    private String id;
    // 名称
    private String name;
    // 关联项目ref
    private String workflowRef;
    // 删除类型
    public VolumeDeletedType deletedType;

    public static Builder aVolumeDeletedEvent() {
        return new Builder();
    }

    @Override
    public LocalDateTime getOccurredTime() {
        return this.occurredTime;
    }

    @Override
    public String getIdentify() {
        return this.identify;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public enum VolumeDeletedType {
        ID,
        NAME,
        REF
    }

    public static class Builder {
        private String id;
        private String name;
        private String workflowRef;
        public VolumeDeletedType deletedType;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder deletedType(VolumeDeletedType deletedType) {
            this.deletedType = deletedType;
            return this;
        }

        public VolumeDeletedEvent build() {
            var event = new VolumeDeletedEvent();
            event.id = this.id;
            event.name = this.name;
            event.workflowRef = this.workflowRef;
            event.deletedType = this.deletedType;
            return event;
        }
    }
}
