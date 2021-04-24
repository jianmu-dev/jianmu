package dev.jianmu.infrastructure.docker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: TaskResult
 * @description: 任务执行结果
 * @author: Ethan Liu
 * @create: 2021-04-16 12:31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskFinishedEvent {
    private String taskId;
    private int cmdStatusCode;
    private String resultFile;
}
