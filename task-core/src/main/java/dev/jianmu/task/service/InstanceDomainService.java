package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;

import java.util.List;

/**
 * @class: InstanceDomainService
 * @description: 任务实例Domain服务
 * @author: Ethan Liu
 * @create: 2021-03-27 09:05
 **/
public class InstanceDomainService {
    public TaskInstance create(
            List<TaskInstance> taskInstances,
            Definition definition,
            String businessId,
            String projectId,
            String asyncTaskRef
    ) {
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
                .defKey(definition.getKey())
                .asyncTaskRef(asyncTaskRef)
                .businessId(businessId)
                .projectId(projectId)
                .outputParameters(definition.getOutputParameters())
                .build();
    }
}
