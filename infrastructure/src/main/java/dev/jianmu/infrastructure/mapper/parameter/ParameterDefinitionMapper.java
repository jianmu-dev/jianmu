package dev.jianmu.infrastructure.mapper.parameter;

import dev.jianmu.parameter.aggregate.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @class: DefinitionMapper
 * @description: 参数定义Mapper
 * @author: Ethan Liu
 * @create: 2021-04-04 11:14
 **/
public interface ParameterDefinitionMapper {
    @Insert("insert into parameter_definition(business_id_scope_ref, name, ref, description, business_id, scope, source, type, value, parameter_type) " +
            "values('${businessId + scope + ref}', #{name}, #{ref}, #{description}, #{businessId}, #{scope}, #{source}, #{type} convert(#{value}, BINARY), '${value.getClass().getSimpleName()}')")
    void add(ParameterDefinition<?> parameterDefinition);

    @Insert("<script>" +
            "insert into parameter_definition(business_id_scope_ref, name, ref, description, business_id, scope, source, type, value, parameter_type) values" +
            "<foreach collection='parameterDefinitions' item='i' index='index' separator=','>" +
            "('${i.businessId + i.scope + i.ref}', #{i.name}, #{i.ref}, #{i.description}, #{i.businessId}, #{i.scope}, #{i.source}, #{i.type}, convert(#{i.value}, BINARY), " +
            "<if test=\"i.value != null\"> '${i.value.getClass().getSimpleName()}') </if>" +
            "<if test=\"i.value == null\"> '') </if>" +
            "</foreach>" +
            " </script>")
    void addList(@Param("parameterDefinitions") List<ParameterDefinition<?>> parameterDefinitions);

    @Delete("delete from parameter_definition where business_id = #{businessId}")
    void deleteByBusinessId(@Param("businessId") String businessId);

    @Select("select * from parameter_definition where business_id = #{businessId} and scope = #{scope}")
    @Result(column = "business_id", property = "businessId")
    @TypeDiscriminator(column = "parameter_type", javaType = String.class, cases = {
            @Case(value = "String", type = StringParameterDefinition.class, constructArgs = {
                    @Arg(column = "value", javaType = String.class)
            }, results = {
                    @Result(column = "value", javaType = String.class)
            }),
            @Case(value = "Boolean", type = BoolParameterDefinition.class, constructArgs = {
                    @Arg(column = "value", javaType = Boolean.class)
            }, results = {
                    @Result(column = "value", javaType = Boolean.class)
            })
    })
    List<? extends ParameterDefinition<?>> findByBusinessIdAndScope(@Param("businessId") String businessId, @Param("scope") String scope);
}
