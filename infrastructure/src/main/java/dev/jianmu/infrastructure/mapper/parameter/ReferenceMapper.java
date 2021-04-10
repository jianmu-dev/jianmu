package dev.jianmu.infrastructure.mapper.parameter;

import dev.jianmu.parameter.aggregate.Reference;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @class: ReferenceMapper
 * @description: 参数引用Mapper
 * @author: Ethan Liu
 * @create: 2021-04-10 10:02
 **/
public interface ReferenceMapper {

    @Insert("<script>" +
            "insert into reference(linked_parameter_id, parameter_id) values" +
            "<foreach collection='references' item='i' index='index' separator=','>" +
            "(#{i.linkedParameterId}, #{i.parameterId})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("references") List<Reference> references);

    @Select("<script>" +
            "select * from reference where linked_parameter_id in " +
            "<foreach collection='linkedParameterIds' item='i' index='index' separator=','>" +
            "(#{i})" +
            "</foreach> " +
            "</script>")
    List<Reference> findByLinkedParameterIds(@Param("linkedParameterIds") Set<String> linkedParameterIds);
}
