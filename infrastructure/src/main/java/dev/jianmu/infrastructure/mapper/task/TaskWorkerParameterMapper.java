package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.mybatis.task.TaskWorkerParameterDO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @class: TaskWorkerParameterMapper
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-04-11 17:23
 **/
public interface TaskWorkerParameterMapper {
    @Insert("<script>" +
            "insert into task_worker_parameter(task_definition_id, parameter_name, parameter_id) values" +
            "<foreach collection='parameterMap' item='value' index='key' separator=','>" +
            "(#{taskDefinitionId}, #{key}, #{value})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("taskDefinitionId") String taskDefinitionId, @Param("parameterMap") Map<String, String> parameterMap);

    @Delete("delete from task_worker_parameter where task_definition_id = #{taskDefinitionId}")
    void deleteByWorkerId(@Param("workerId") String taskDefinitionId);

    @Select("select * from task_worker_parameter where task_definition_id = #{taskDefinitionId}")
    @Result(column = "task_definition_id", property = "taskDefinitionId")
    @Result(column = "parameter_name", property = "parameterKey")
    @Result(column = "parameter_id", property = "parameterValue")
    List<TaskWorkerParameterDO> findByTaskDefinitionId(@Param("taskDefinitionId") String taskDefinitionId);
}
