package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.aggregate.TriggerParameter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * @class: TriggerParameterMapper
 * @description: 触发器参数Mapper
 * @author: Ethan Liu
 * @create: 2021-04-11 23:14
 **/
public interface TriggerParameterMapper {
    @Insert("<script>" +
            "insert into trigger_parameter(trigger_id, name, ref, type, description, parameterId) values" +
            "<foreach collection='parameters' item='i' index='index' separator=','>" +
            "(#{triggerId}, #{i.name}, #{i.ref}, #{i.type}, #{i.description}, #{i.parameterId})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("triggerId") String triggerId, @Param("parameters") Set<TriggerParameter> parameters);

    @Delete("delete from trigger_parameter where trigger_id = #{triggerId}")
    void deleteByTriggerId(@Param("taskInstanceId") String triggerId);

    @Select("select * from trigger_parameter where trigger_id = #{triggerId}")
    Set<TriggerParameter> findByTriggerId(@Param("triggerId") String triggerId);
}
