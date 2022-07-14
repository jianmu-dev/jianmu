package dev.jianmu.infrastructure.mapper.external_parameter;

import dev.jianmu.external_parameter.aggregate.ExternalParameter;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterRepositoryImpl
 * @description ExternalParameterRepositoryImpl
 * @create 2022-07-13 15:22
 */
public interface ExternalParameterMapper {
    @Insert("insert into jm_external_parameter(id, ref, name, type, value, label, association_id, association_type, created_time, last_modified_time) " +
            "values(#{id}, #{ref}, #{name}, #{type}, #{value}, #{label},#{associationId}, #{associationType}, #{createdTime}, #{lastModifiedTime})")
    void add(ExternalParameter externalParameter);

    @Delete("delete from jm_external_parameter where id = #{id}")
    void deleteById(String id);

    @Update("update jm_external_parameter set name = #{name}, value = #{value}, label = #{label}, last_modified_time = #{lastModifiedTime}, type = #{type} where id = #{id}")
    void updateById(ExternalParameter externalParameter);

    @Select("<script>" +
            "SELECT * FROM `jm_external_parameter`" +
            "<where> id = #{id}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    @Result(column = "id", property = "id")
    @Result(column = "ref", property = "ref")
    @Result(column = "name", property = "name")
    @Result(column = "label", property = "label")
    @Result(column = "type", property = "type")
    @Result(column = "value", property = "value")
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    Optional<ExternalParameter> findById(@Param("id") String id,
                                         @Param("associationId") String associationId,
                                         @Param("associationType") String associationType);

    @Select("<script>" +
            "SELECT * FROM `jm_external_parameter`" +
            "<where>" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    @Result(column = "id", property = "id")
    @Result(column = "ref", property = "ref")
    @Result(column = "name", property = "name")
    @Result(column = "label", property = "label")
    @Result(column = "type", property = "type")
    @Result(column = "value", property = "value")
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    List<ExternalParameter> findAll(@Param("associationId") String associationId,
                                    @Param("associationType") String associationType);
}
