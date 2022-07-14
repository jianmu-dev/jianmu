package dev.jianmu.infrastructure.mapper.external_parameter;

import dev.jianmu.external_parameter.aggregate.ExternalParameterLabel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterLabelRepositoryImpl
 * @description ExternalParameterLabelRepositoryImpl
 * @create 2022-07-13 15:22
 */
public interface ExternalParameterLabelMapper {
    @Insert("insert into jm_external_parameter_label(id, value, association_id, association_type, created_time, last_modified_time) " +
            "values(#{id}, #{value}, #{associationId}, #{associationType}, #{createdTime}, #{lastModifiedTime})")
    void add(ExternalParameterLabel externalParameterLabel);


    @Select("<script>" +
            "SELECT * FROM `jm_external_parameter_label`" +
            "<where>" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    @Result(column = "id", property = "id")
    @Result(column = "value", property = "value")
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    @Result(column = "created_time", property = "createdTime")
    List<ExternalParameterLabel> findAll(@Param("associationId") String associationId,
                                         @Param("associationType") String associationType);

    @Select("<script>" +
            "SELECT * FROM `jm_external_parameter_label`" +
            "<where> value = #{value}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    @Result(column = "id", property = "id")
    @Result(column = "value", property = "value")
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    @Result(column = "created_time", property = "createdTime")
    Optional<ExternalParameterLabel> findByValue(
            @Param("associationId") String associationId,
            @Param("associationType") String associationType,
            @Param("value") String value
    );
}
