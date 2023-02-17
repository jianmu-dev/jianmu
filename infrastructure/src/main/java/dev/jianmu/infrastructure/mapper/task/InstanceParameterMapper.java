package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.InstanceParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @class InstanceParameterMapper
 * @description InstanceParameterMapper
 * @author Ethan Liu
 * @create 2021-05-01 21:31
*/
public interface InstanceParameterMapper {
    @Insert("<script>" +
            "insert into task_instance_parameter(instance_id, serial_no, def_key, async_task_ref, business_id, trigger_id, ref, `type`, workflow_type, parameter_id, required) values" +
            "<foreach collection='instanceParameters' item='i' index='key' separator=','>" +
            "(#{i.instanceId}, #{i.serialNo}, #{i.defKey}, #{i.asyncTaskRef}, #{i.businessId}, #{i.triggerId}, #{i.ref}, #{i.type}, #{i.workflowType}, #{i.parameterId}, #{i.required})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("instanceParameters") Set<InstanceParameter> instanceParameters);

    @Select("select * from task_instance_parameter where instance_id = #{instanceId}")
    @Result(column = "instance_id", property = "instanceId")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "parameter_id", property = "parameterId")
    @Result(column = "workflow_type", property = "workflowType")
    List<InstanceParameter> findByInstanceId(@Param("instanceId") String instanceId);

    @Select("select * from task_instance_parameter where instance_id = #{instanceId} and type = #{type}")
    @Result(column = "instance_id", property = "instanceId")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "parameter_id", property = "parameterId")
    @Result(column = "workflow_type", property = "workflowType")
    List<InstanceParameter> findByInstanceIdAndType(@Param("instanceId") String instanceId, @Param("type") InstanceParameter.Type type);

    @Select("SELECT T.* FROM task_instance_parameter as T," +
            "(" +
            "SELECT max(serial_no) as max_no, async_task_ref, ref FROM task_instance_parameter " +
            "    WHERE trigger_id=#{triggerId} " +
            "    GROUP BY  async_task_ref, ref" +
            ") as B " +
            "WHERE trigger_id=#{triggerId} " +
            "AND T.async_task_ref=B.async_task_ref AND T.ref=B.ref AND T.serial_no=B.max_no AND T.type='OUTPUT'")
    @Result(column = "instance_id", property = "instanceId")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "parameter_id", property = "parameterId")
    @Result(column = "workflow_type", property = "workflowType")
    List<InstanceParameter> findLastOutputParamByTriggerId(String triggerId);
}
