package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.TaskResultDto;
import dev.jianmu.infrastructure.docker.TaskResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @class: TaskResultMapper
 * @description: TaskResultMapper
 * @author: Ethan Liu
 * @create: 2021-04-24 15:24
 **/
@Mapper
public interface TaskResultMapper {
    TaskResultMapper INSTANCE = Mappers.getMapper(TaskResultMapper.class);

    @Mapping(target = "taskInstanceId", source = "taskId")
    @Mapping(target = "succeeded", expression = "java(taskResult.getStatusCode() != 0)")
    TaskResultDto toTaskResultDto(TaskResult taskResult);
}
