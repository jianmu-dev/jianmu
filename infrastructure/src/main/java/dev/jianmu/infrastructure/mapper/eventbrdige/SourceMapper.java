package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Source;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

/**
 * @class: SourceMapper
 * @description: SourceMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 09:07
 **/
public interface SourceMapper {
    @Select("SELECT * FROM `eb_source` WHERE id = #{id}")
    Optional<Source> findById(String id);

    @Update("update eb_source set token = #{token} WHERE id = #{id}")
    void updateTokenById(Source source);

    @Insert("insert into eb_source(id, name, type, token) " +
            "values(#{id}, #{name}, #{type}, #{token})")
    void save(Source source);

    @Delete("delete FROM eb_source WHERE id = #{id}")
    void deleteById(String id);
}
