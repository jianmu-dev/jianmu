package dev.jianmu.infrastructure.mapper.externalParameter;

import dev.jianmu.externalParameter.aggregate.ExternalParameterLabel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterLabelRepositoryImpl
 * @description ExternalParameterLabelRepositoryImpl
 * @create 2022-07-13 15:22
 */
public interface ExternalParameterLabelMapper {
    @Insert("insert into external_parameter_label(id, value) " +
            "values(#{id}, #{value})")
    void add(ExternalParameterLabel externalParameterLabel);


    @Select("SELECT * FROM `external_parameter_label`")
    @Result(column = "id", property = "id")
    @Result(column = "value", property = "value")
    List<ExternalParameterLabel> findAll();

    @Delete("delete from external_parameter_label where id = #{id}")
    void deleteById(String id);

    @Update("update external_parameter_label set value = #{value} where id = #{id}")
    void updateById(ExternalParameterLabel externalParameterLabel);

    @Select("SELECT * FROM `external_parameter_label` WHERE id = #{id}")
    @Result(column = "id", property = "id")
    @Result(column = "value", property = "value")
    Optional<ExternalParameterLabel> findById(String id);

    @Select("SELECT * FROM `external_parameter_label` WHERE value = #{value}")
    @Result(column = "id", property = "id")
    @Result(column = "value", property = "value")
    Optional<ExternalParameterLabel> findByValue(String value);
}
