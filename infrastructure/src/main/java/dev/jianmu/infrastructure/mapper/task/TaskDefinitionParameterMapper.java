package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.TaskParameter;
import org.apache.ibatis.annotations.*;

import java.util.Set;

/**
 * @class: TaskDefinitionParameterMapper
 * @description: 任务定义参数表Mapper
 * @author: Ethan Liu
 * @create: 2021-04-11 12:41
 **/
public interface TaskDefinitionParameterMapper {
    @Insert("<script>" +
            "insert into task_parameter(definition_id, name, ref, type, description, parameterId) values" +
            "<foreach collection='parameters' item='i' index='index' separator=','>" +
            "(#{taskDefinitionId}, #{i.name}, #{i.ref}, #{i.type}, #{i.description}, #{i.parameterId})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("taskDefinitionId") String taskDefinitionId, @Param("parameters") Set<TaskParameter> parameters);

    @Delete("delete from task_parameter where definition_id = #{taskDefinitionId}")
    void deleteByTaskDefinitionId(@Param("taskDefinitionId") String taskDefinitionId);

    @Select("select * from task_parameter where definition_id = #{taskDefinitionId}")
    Set<TaskParameter> findByTaskDefinitionId(@Param("taskDefinitionId") String taskDefinitionId);
}
