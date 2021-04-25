package dev.jianmu.infrastructure.mapper.dsl;

import dev.jianmu.dsl.aggregate.OutputParameterRefer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @class: OutputParameterReferMapper
 * @description: OutputParameterReferMapper
 * @author: Ethan Liu
 * @create: 2021-04-25 14:28
 **/
public interface OutputParameterReferMapper {
    @Insert("<script>" +
            "insert into dsl_output_parameter_refer(project_id, workflow_version, output_node_name, output_node_type, output_parameter_ref, output_parameter_id," +
            " input_node_name, input_node_type, input_parameter_ref, input_parameter_id, context_id) values" +
            "<foreach collection='refers' item='i' index='index' separator=','>" +
            "(#{i.projectId}, #{i.workflowVersion}, #{i.outputNodeName}, #{i.outputNodeType}, #{i.outputParameterRef}, #{i.outputParameterId}, " +
            "#{i.inputNodeName}, #{i.inputNodeType}, #{i.inputParameterRef}, #{i.inputParameterId}, #{i.contextId})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("refers") Set<OutputParameterRefer> refers);

    @Select("select * from dsl_output_parameter_refer where context_id = #{contextId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "output_node_name", property = "outputNodeName")
    @Result(column = "output_node_type", property = "outputNodeType")
    @Result(column = "output_parameter_ref", property = "outputParameterRef")
    @Result(column = "output_parameter_id", property = "outputParameterId")
    @Result(column = "input_node_name", property = "inputNodeName")
    @Result(column = "input_node_type", property = "inputNodeType")
    @Result(column = "input_parameter_ref", property = "inputParameterRef")
    @Result(column = "input_parameter_id", property = "inputParameterId")
    @Result(column = "context_id", property = "contextId")
    List<OutputParameterRefer> findByContextId(String contextId);
}
