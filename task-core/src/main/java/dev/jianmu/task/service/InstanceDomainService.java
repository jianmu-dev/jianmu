package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.InstanceStatus;

import java.util.List;

/**
 * @class: InstanceDomainService
 * @description: 任务实例Domain服务
 * @author: Ethan Liu
 * @create: 2021-03-27 09:05
 **/
public class InstanceDomainService {
    public TaskInstance create(List<TaskInstance> taskInstances, TaskDefinition taskDefinition, String businessId) {
        if (taskInstances.size() > 0) {
            boolean isRunning = taskInstances.stream()
                    .anyMatch(instance ->
                                    instance.getStatus().equals(InstanceStatus.WAITING) ||
                                    instance.getStatus().equals(InstanceStatus.RUNNING)
                    );
            if (isRunning) {
                throw new RuntimeException("已有任务运行中，不能重复触发");
            }
        }
        return TaskInstance.Builder.anInstance()
                .name(taskDefinition.getName())
                .description(taskDefinition.getDescription())
                .defKey(taskDefinition.getKey())
                .defVersion(taskDefinition.getVersion())
                .businessId(businessId)
                .build();
    }
}
