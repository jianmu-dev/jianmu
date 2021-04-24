package dev.jianmu.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: TaskResult
 * @description: 任务执行结果DTO
 * @author: Ethan Liu
 * @create: 2021-04-16 12:50
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResultDto {
    public enum Status {
        SUCCEEDED,
        FAILED
    }
    private String taskInstanceId;
    private int cmdStatusCode;
    private Status status;
}
