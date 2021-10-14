package dev.jianmu.infrastructure.docker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: TaskFailedEvent
 * @description: TaskFailedEvent
 * @author: Ethan Liu
 * @create: 2021-05-06 09:19
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskFailedEvent {
    private String triggerId;
    private String taskId;
    private String errorMsg;
}
