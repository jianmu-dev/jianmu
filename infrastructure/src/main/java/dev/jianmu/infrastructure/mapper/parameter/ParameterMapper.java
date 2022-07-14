package dev.jianmu.infrastructure.mapper.parameter;

import dev.jianmu.workflow.aggregate.parameter.*;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @class ParameterMapper
 * @description 参数Mapper
 * @author Ethan Liu
 * @create 2021-04-10 10:02
*/
public interface ParameterMapper {
    @Insert("<script>" +
            "insert into jm_parameter(id, type, value) values" +
            "<foreach collection='parameters' item='i' index='index' separator=','>" +
            "(#{i.id}, #{i.type}, convert(#{i.value}, BINARY))" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("parameters") List<Parameter> parameters);

    @Select("<script>" +
            "SELECT * FROM `jm_parameter` WHERE `id` IN" +
            "<foreach collection='ids' item='item' open='(' separator=',' close=')'> #{item}" +
            "</foreach>" +
            "</script>")
    @TypeDiscriminator(column = "type", javaType = String.class, cases = {
            @Case(value = "STRING", type = StringParameter.class, constructArgs = {
                    @Arg(column = "value", javaType = String.class)
            }, results = {
                    @Result(column = "value", javaType = String.class)
            }),
            @Case(value = "SECRET", type = SecretParameter.class, constructArgs = {
                    @Arg(column = "value", javaType = String.class)
            }, results = {
                    @Result(column = "value", javaType = String.class)
            }),
            @Case(value = "NUMBER", type = NumberParameter.class, constructArgs = {
                    @Arg(column = "value", javaType = BigDecimal.class)
            }, results = {
                    @Result(column = "value", javaType = BigDecimal.class)
            }),
            @Case(value = "BOOL", type = BoolParameter.class, constructArgs = {
                    @Arg(column = "value", javaType = Boolean.class)
            }, results = {
                    @Result(column = "value", javaType = Boolean.class)
            })
    })
    List<Parameter> findByIds(@Param("ids") Set<String> ids);
}
