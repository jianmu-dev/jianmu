package dev.jianmu.eventbridge.aggregate.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class: BaseEvent
 * @description: BaseEvent
 * @author: Ethan Liu
 * @create: 2021-10-22 03:41
 **/
public abstract class BaseEvent {
    // 触发时间
    private final LocalDateTime occurredTime;
    // 事件唯一ID
    private final String id;

    protected BaseEvent() {
        this.occurredTime = LocalDateTime.now();
        // TODO 暂时使用UUID的值
        this.id = UUID.randomUUID().toString().replace("-", "");
    }

    public LocalDateTime getOccurredTime() {
        return occurredTime;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "occurredTime=" + occurredTime +
                ", id='" + id + '\'' +
                '}';
    }
}
