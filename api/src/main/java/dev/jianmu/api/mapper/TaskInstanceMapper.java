package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.TaskInstanceVo;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(target = "taskVersion", source = "version.name")
    @Mapping(target = "taskName", source = "definition.name")
    @Mapping(target = "nodeName", source = "taskInstance.asyncTaskKey")
    @Mapping(target = "instanceId", source = "taskInstance.id")
    TaskInstanceVo toTaskInstanceVo(TaskInstance taskInstance, TaskDefinition definition, TaskDefinitionVersion version);
}
