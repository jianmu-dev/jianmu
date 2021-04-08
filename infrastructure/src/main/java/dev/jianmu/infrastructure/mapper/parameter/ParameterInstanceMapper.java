package dev.jianmu.infrastructure.mapper.parameter;

import dev.jianmu.parameter.aggregate.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @class: InstanceMapper
 * @description: 参数实例Mapper
 * @author: Ethan Liu
 * @create: 2021-04-04 11:14
 **/
public interface ParameterInstanceMapper {
    @Insert("")
    void add(ParameterInstance<?> parameterInstance);

    @Insert("<script>" +
            "insert into parameter_instance(business_id_scope_ref, name, ref, description, business_id, scope, type, value, parameter_type) values" +
            "<foreach collection='parameterInstances' item='i' index='index' separator=','>" +
            "('${i.businessId + i.scope + i.ref}', #{i.name}, #{i.ref}, #{i.description}, #{i.businessId}, #{i.scope}, #{i.type}, convert(#{i.value}, BINARY), " +
            "<if test=\"i.value != null\"> '${i.value.getClass().getSimpleName()}') </if>" +
            "<if test=\"i.value == null\"> '') </if>" +
            "</foreach>" +
            " </script>")
    void addList(@Param("parameterInstances") List<ParameterInstance<?>> parameterInstances);

    @Select("select * from parameter_instance where business_id = #{businessId} and scope = #{scope}")
    @Result(column = "business_id", property = "businessId")
    @TypeDiscriminator(column = "parameter_type", javaType = String.class, cases = {
            @Case(value = "String", type = StringParameterInstance.class, constructArgs = {
                    @Arg(column = "value", javaType = String.class)
            }, results = {
                    @Result(column = "value", javaType = String.class)
            }),
            @Case(value = "Boolean", type = BoolParameterInstance.class, constructArgs = {
                    @Arg(column = "value", javaType = Boolean.class)
            }, results = {
                    @Result(column = "value", javaType = Boolean.class)
            })
    })
    List<? extends ParameterInstance<?>> findByBusinessIdAndScope(@Param("businessId") String businessId, @Param("scope") String scope);
}
