package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.TaskDefinitionDto;
import dev.jianmu.api.dto.TaskDefinitionVersionDto;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @class: TaskDefinitionVersionDtoMapper
 * @description: 任务定义版本DTO Mapper
 * @author: Ethan Liu
 * @create: 2021-04-20 14:42
 **/
@Mapper
public interface TaskDefinitionDtoMapper {
    TaskDefinitionDtoMapper INSTANCE = Mappers.getMapper(TaskDefinitionDtoMapper.class);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "ref", target = "ref")
    TaskDefinition toTaskDefinition(TaskDefinitionDto taskDefinitionDto);

    @Mapping(target = "definitionKey", ignore = true)
    @Mapping(source = "ref", target = "taskDefinitionRef")
    @Mapping(source = "version", target = "name")
    @Mapping(source = "description", target = "description")
    TaskDefinitionVersion toTaskDefinitionVersion(TaskDefinitionVersionDto taskDefinitionVersionDto);

    @Mapping(target = "version", source = "taskDefinitionVersion.name")
    @Mapping(target = "ref", source = "dockerDefinition.key")
    @Mapping(target = "name", source = "taskDefinition.name")
    @Mapping(target = "type", source = "dockerDefinition.type")
    @Mapping(target = "createdTime", source = "taskDefinition.createdTime")
    @Mapping(target = "lastModifiedTime", source = "taskDefinition.lastModifiedTime")
    TaskDefinitionDto toTaskDefinitionDto(DockerDefinition dockerDefinition, TaskDefinitionVersion taskDefinitionVersion, TaskDefinition taskDefinition);
}
