package dev.jianmu.infrastructure.worker.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class TaskRunningEvent
 * @description 任务开始运行事件
 * @author Ethan Liu
 * @create 2021-04-24 18:12
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskRunningEvent {
    private String taskId;
    private String workerId;
}
