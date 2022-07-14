package dev.jianmu.infrastructure.mapper.externalParameter;

import dev.jianmu.externalParameter.aggregate.ExternalParameter;
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
    @Insert("insert into external_parameter(id, ref, name, type, value, label) " +
            "values(#{id}, #{ref}, #{name}, #{type}, #{value}, #{label})")
    void add(ExternalParameter externalParameter);

    @Delete("delete from external_parameter where id = #{id}")
    void deleteById(String id);

    @Update("update external_parameter set name = #{name}, value = #{value}, label = #{label}, type = #{type} where id = #{id}")
    void updateById(ExternalParameter externalParameter);

    @Select("SELECT * FROM `external_parameter` WHERE id = #{id}")
    @Result(column = "id", property = "id")
    @Result(column = "ref", property = "ref")
    @Result(column = "name", property = "name")
    @Result(column = "label", property = "label")
    @Result(column = "type", property = "type")
    @Result(column = "value", property = "value")
    Optional<ExternalParameter> findById(String id);

    @Select("SELECT * FROM `external_parameter`")
    @Result(column = "id", property = "id")
    @Result(column = "ref", property = "ref")
    @Result(column = "name", property = "name")
    @Result(column = "label", property = "label")
    @Result(column = "type", property = "type")
    @Result(column = "value", property = "value")
    List<ExternalParameter> findAll();
}
