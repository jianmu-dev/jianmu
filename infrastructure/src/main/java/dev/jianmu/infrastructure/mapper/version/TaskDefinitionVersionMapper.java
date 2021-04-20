package dev.jianmu.infrastructure.mapper.version;

import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: TaskDefinitionVersionMapper
 * @description: 任务定义版本Mapper
 * @author: Ethan Liu
 * @create: 2021-04-18 16:06
 **/
public interface TaskDefinitionVersionMapper {
    @Insert("insert into task_definition_version(task_definition_id, name, task_definition_ref, definition_key, description) " +
            "values(#{taskDefinitionId}, #{name}, #{taskDefinitionRef}, #{definitionKey}, #{description})")
    void add(TaskDefinitionVersion taskDefinitionVersion);

    @Delete("delete from task_definition_version where task_definition_ref = #{taskDefinitionRef}")
    void delete(TaskDefinitionVersion taskDefinitionVersion);

    @Update("update task_definition_version set description = #{description} " +
            "where definition_key = #{definition_key}")
    void updateDescription(TaskDefinitionVersion taskDefinitionVersion);

    @Select("select * from task_definition_version where task_definition_ref = #{taskDefinitionRef}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "task_definition_ref", property = "taskDefinitionRef")
    @Result(column = "definition_key", property = "definitionKey")
    List<TaskDefinitionVersion> findByTaskDefinitionRef(String taskDefinitionRef);

    @Select("select * from task_definition_version where task_definition_id = #{taskDefinitionId} and name = #{name}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "task_definition_ref", property = "taskDefinitionRef")
    @Result(column = "definition_key", property = "definitionKey")
    Optional<TaskDefinitionVersion> findByTaskDefinitionRefAndName(String taskDefinitionRef, String name);

    @Select("select * from task_definition_version where definition_key = #{key}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "task_definition_ref", property = "taskDefinitionRef")
    @Result(column = "definition_key", property = "definitionKey")
    Optional<TaskDefinitionVersion> findByDefinitionKey(String key);
}
