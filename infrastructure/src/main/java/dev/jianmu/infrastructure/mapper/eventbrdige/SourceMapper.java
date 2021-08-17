package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Source;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

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

    @Insert("insert into eb_source(id, name, type) " +
            "values(#{id}, #{name}, #{type})")
    void save(Source source);
}
