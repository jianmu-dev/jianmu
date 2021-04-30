package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.ParameterRefer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class: ParameterReferMapper
 * @description: ParameterReferMapper
 * @author: Ethan Liu
 * @create: 2021-04-30 12:38
 **/
public interface ParameterReferMapper {
    @Insert("insert into parameter_refer(workflow_ref, workflow_version, source_task_ref, source_parameter_ref, target_task_ref, target_parameter_ref) " +
            "values(#{workflowRef}, #{workflowVersion}, #{sourceTaskRef}, #{sourceParameterRef}, #{targetTaskRef}, #{targetParameterRef})")
    void addAll(List<ParameterRefer> parameterRefers);

    @Select("select * from parameter_refer where workflow_ref = #{workflowRef} and workflow_version = #{workflowVersion} and target_task_ref = #{targetTaskRef}")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "source_task_ref", property = "sourceTaskRef")
    @Result(column = "source_parameter_ref", property = "sourceParameterRef")
    @Result(column = "target_task_ref", property = "targetTaskRef")
    @Result(column = "target_parameter_ref", property = "targetParameterRef")
    List<ParameterRefer> findByRefAndVersionAndTargetTaskRef(String workflowRef, String workflowVersion, String targetTaskRef);
}
