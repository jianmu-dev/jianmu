package dev.jianmu.task.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @class DomainEvent
 * @description 领域事件接口
 * @author Ethan Liu
 * @create 2021-03-25 15:50
*/
public interface DomainEvent extends Serializable {
    LocalDateTime getOccurredTime();
    String getIdentify();
}
