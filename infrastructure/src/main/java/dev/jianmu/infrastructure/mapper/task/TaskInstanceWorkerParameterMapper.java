package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.mybatis.task.TaskInstanceWorkerParameterDO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @class: TaskInstanceWorkerParameterMapper
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-04-11 19:21
 **/
public interface TaskInstanceWorkerParameterMapper {
    @Insert("<script>" +
            "insert into task_instance_worker_parameter(task_instance_id, parameter_name, parameter_id) values" +
            "<foreach collection='parameterMap' item='value' index='key' separator=','>" +
            "(#{taskInstanceId}, #{key}, #{value})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("taskInstanceId") String taskInstanceId, @Param("parameterMap") Map<String, String> parameterMap);

    @Delete("delete from task_instance_worker_parameter where task_instance_id = #{taskInstanceId}")
    void deleteByTaskInstanceId(@Param("taskInstanceId") String taskInstanceId);

    @Select("select * from task_instance_worker_parameter where task_instance_id = #{taskInstanceId}")
    @Result(column = "task_instance_id", property = "taskInstanceId")
    @Result(column = "parameter_name", property = "parameterKey")
    @Result(column = "parameter_id", property = "parameterValue")
    List<TaskInstanceWorkerParameterDO> findByTaskInstanceId(@Param("taskInstanceId") String taskInstanceId);
}
