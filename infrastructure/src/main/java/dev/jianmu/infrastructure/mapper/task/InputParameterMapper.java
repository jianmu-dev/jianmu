package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.InputParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class: InputParameterMapper
 * @description: InputParameterMapper
 * @author: Ethan Liu
 * @create: 2021-04-30 12:39
 **/
public interface InputParameterMapper {
    @Insert("insert into input_parameter(def_key, async_task_ref, workflow_ref, workflow_version, project_id, ref, parameter_id) " +
            "values(#{defKey}, #{asyncTaskRef}, #{workflowRef}, #{workflowVersion}, #{projectId}, #{ref}, #{parameterId})")
    void addAll(List<InputParameter> inputParameters);

    @Select("select * from input_parameter where workflow_ref = #{workflowRef} and workflow_version = #{workflowVersion} and async_task_ref = #{asyncTaskRef}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "parameter_id", property = "parameterId")
    List<InputParameter> findByWorkflowRefAndWorkflowVersionAndAsyncTaskRef(String workflowRef, String workflowVersion, String asyncTaskRef);
}
