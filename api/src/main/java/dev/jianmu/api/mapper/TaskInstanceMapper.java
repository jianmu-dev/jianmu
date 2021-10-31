package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.TaskInstanceVo;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.mapstruct.factory.Mappers;

/**
 * @class: TaskInstanceMapper
 * @description: 任务实例Mapper
 * @author: Ethan Liu
 * @create: 2021-04-22 13:53
 **/
@Mapper
public interface TaskInstanceMapper {
    TaskInstanceMapper INSTANCE = Mappers.getMapper(TaskInstanceMapper.class);

    @Mapping(target = "instanceId", source = "id")
    @Mapping(target = "nodeName", source = "asyncTaskRef")
    TaskInstanceVo toTaskInstanceVo(TaskInstance taskInstance);

    @Mapping(target = "defKey", source = "asyncTaskType")
    @Mapping(target = "nodeName", source = "asyncTaskRef")
    TaskInstanceVo asyncTaskInstanceToTaskInstanceVo(AsyncTaskInstance asyncTaskInstance);

    @ValueMappings({
            @ValueMapping(target = "WAITING", source = "WAITING"),
            @ValueMapping(target = "RUNNING", source = "RUNNING"),
            @ValueMapping(target = "FAILED", source = "EXECUTION_FAILED"),
            @ValueMapping(target = "FAILED", source = "DISPATCH_FAILED"),
            @ValueMapping(target = "SUCCEEDED", source = "EXECUTION_SUCCEEDED")
    })
    TaskInstanceVo.Status toTaskInstanceStatus(InstanceStatus instanceStatus);

    @ValueMappings({
            @ValueMapping(target = "INIT", source = "INIT"),
            @ValueMapping(target = "RUNNING", source = "RUNNING"),
            @ValueMapping(target = "SKIPPED", source = "SKIPPED"),
            @ValueMapping(target = "FAILED", source = "FAILED"),
            @ValueMapping(target = "SUCCEEDED", source = "SUCCEEDED")
    })
    TaskInstanceVo.Status toTaskInstanceStatus(TaskStatus taskStatus);
}
