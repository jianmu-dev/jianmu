package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.InstanceParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @class: InstanceParameterMapper
 * @description: InstanceParameterMapper
 * @author: Ethan Liu
 * @create: 2021-05-01 21:31
 **/
public interface InstanceParameterMapper {
    @Insert("<script>" +
            "insert into task_instance_parameter(instance_id, serial_no, def_key, async_task_ref, business_id, project_id, ref, `type`, parameter_id) values" +
            "<foreach collection='instanceParameters' item='i' index='key' separator=','>" +
            "(#{i.instanceId}, #{i.serialNo}, #{i.defKey}, #{i.asyncTaskRef}, #{i.businessId}, #{i.projectId}, #{i.ref}, #{i.type}, #{i.parameterId})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("instanceParameters") Set<InstanceParameter> instanceParameters);

    @Select("select * from task_instance_parameter where business_id = #{businessId}")
    @Result(column = "instance_id", property = "instanceId")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "parameter_id", property = "parameterId")
    List<InstanceParameter> findByBusinessId(String businessId);

    @Select("select * from task_instance_parameter where instance_id = #{instanceId} and type = #{type}")
    @Result(column = "instance_id", property = "instanceId")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "parameter_id", property = "parameterId")
    List<InstanceParameter> findByInstanceIdAndType(@Param("instanceId") String instanceId, @Param("type") InstanceParameter.Type type);

    @Select("select * from task_instance_parameter where business_id = #{businessId} and async_task_ref = #{asyncTaskRef} and ref = #{ref}")
    @Result(column = "instance_id", property = "instanceId")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "parameter_id", property = "parameterId")
    Optional<InstanceParameter> findInputParamByBusinessIdAndTaskRefAndRefAndMaxSerial(@Param("businessId") String businessId, @Param("asyncTaskRef") String asyncTaskRef, @Param("ref") String ref);
}