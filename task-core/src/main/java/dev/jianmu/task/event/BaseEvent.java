package dev.jianmu.task.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class: BaseEvent
 * @description: 领域事件抽象类
 * @author: Ethan Liu
 * @create: 2021-03-25 15:50
 **/
public abstract class BaseEvent implements DomainEvent {
    // 触发时间
    private LocalDateTime occurredTime;
    // 事件唯一ID
    private String identify;

    // 事件名称
    private String name = this.getClass().getSimpleName();
    // 任务实例ID
    protected String taskInstanceId;
    // 任务定义唯一Key
    protected String defKey;
    // 流程定义上下文中的AsyncTask唯一标识
    protected String asyncTaskRef;
    // 外部业务ID, 必须唯一
    protected String businessId;

    protected BaseEvent() {
        this.occurredTime = LocalDateTime.now();
        // TODO 暂时使用UUID的值
        this.identify = UUID.randomUUID().toString().replace("-", "");
    }

    public String getName() {
        return name;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public String getDefKey() {
        return defKey;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getBusinessId() {
        return businessId;
    }

    @Override
    public LocalDateTime getOccurredTime() {
        return occurredTime;
    }

    @Override
    public String getIdentify() {
        return identify;
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "occurredTime=" + occurredTime +
                ", identify='" + identify + '\'' +
                ", name='" + name + '\'' +
                ", taskInstanceId='" + taskInstanceId + '\'' +
                ", defKey='" + defKey + '\'' +
                ", asyncTaskRef='" + asyncTaskRef + '\'' +
                ", businessId='" + businessId + '\'' +
                '}';
    }
}
