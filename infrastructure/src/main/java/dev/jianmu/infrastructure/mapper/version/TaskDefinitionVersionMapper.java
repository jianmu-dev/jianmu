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
    @Insert("insert into task_definition_version(task_definition_id, task_definition_name, name, task_definition_ref, definition_key, description, created_time, last_modified_time) " +
            "values(#{taskDefinitionId}, #{taskDefinitionName}, #{name}, #{taskDefinitionRef}, #{definitionKey}, #{description}, #{createdTime}, #{lastModifiedTime})")
    void add(TaskDefinitionVersion taskDefinitionVersion);

    @Delete("delete from task_definition_version where definition_key = #{definitionKey}")
    void delete(TaskDefinitionVersion taskDefinitionVersion);

    @Update("update task_definition_version set description = #{description} " +
            "where definition_key = #{definition_key}")
    void updateDescription(TaskDefinitionVersion taskDefinitionVersion);

    @Select("select * from task_definition_version where task_definition_ref = #{taskDefinitionRef}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "task_definition_name", property = "taskDefinitionName")
    @Result(column = "task_definition_ref", property = "taskDefinitionRef")
    @Result(column = "definition_key", property = "definitionKey")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<TaskDefinitionVersion> findByTaskDefinitionRef(String taskDefinitionRef);

    @Select("select * from task_definition_version where task_definition_ref = #{taskDefinitionRef} and name = #{name}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "task_definition_name", property = "taskDefinitionName")
    @Result(column = "task_definition_ref", property = "taskDefinitionRef")
    @Result(column = "definition_key", property = "definitionKey")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<TaskDefinitionVersion> findByTaskDefinitionRefAndName(@Param("taskDefinitionRef") String taskDefinitionRef, @Param("name") String name);

    @Select("select * from task_definition_version where definition_key = #{key}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "task_definition_name", property = "taskDefinitionName")
    @Result(column = "task_definition_ref", property = "taskDefinitionRef")
    @Result(column = "definition_key", property = "definitionKey")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<TaskDefinitionVersion> findByDefinitionKey(String key);
}
