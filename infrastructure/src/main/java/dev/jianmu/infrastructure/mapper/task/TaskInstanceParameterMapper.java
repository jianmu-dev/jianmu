package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.TaskParameter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * @class: TaskInstanceParameterMapper
 * @description: 任务实例参数Mapper
 * @author: Ethan Liu
 * @create: 2021-04-11 19:26
 **/
public interface TaskInstanceParameterMapper {
    @Insert("<script>" +
            "insert into task_instance_parameter(instance_id, name, ref, type, description, parameterId) values" +
            "<foreach collection='parameters' item='i' index='index' separator=','>" +
            "(#{taskInstanceId}, #{i.name}, #{i.ref}, #{i.type}, #{i.description}, #{i.parameterId})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("taskInstanceId") String taskInstanceId, @Param("parameters") Set<TaskParameter> parameters);

    @Delete("delete from task_instance_parameter where instance_id = #{taskInstanceId}")
    void deleteByTaskInstanceId(@Param("taskInstanceId") String taskInstanceId);

    @Select("select * from task_instance_parameter where instance_id = #{taskInstanceId}")
    Set<TaskParameter> findByTaskInstanceId(@Param("taskInstanceId") String taskInstanceId);
}
