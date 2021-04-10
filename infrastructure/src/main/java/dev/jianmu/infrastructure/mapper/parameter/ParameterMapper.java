package dev.jianmu.infrastructure.mapper.parameter;

import dev.jianmu.parameter.aggregate.Parameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @class: ParameterMapper
 * @description: 参数Mapper
 * @author: Ethan Liu
 * @create: 2021-04-10 10:02
 **/
public interface ParameterMapper {
    @Insert("<script>" +
            "insert into parameter(id, type, value) values" +
            "<foreach collection='parameters' item='i' index='index' separator=','>" +
            "(#{i.id}, #{i.type}, #{i.value})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("parameters") List<Parameter> parameters);

    @Select("<script>" +
            "select * from parameter where id in " +
            "<foreach collection='ids' item='i' index='index' separator=','>" +
            "(#{i})" +
            "</foreach> " +
            "</script>")
    List<Parameter> findByIds(@Param("ids") Set<String> ids);
}
