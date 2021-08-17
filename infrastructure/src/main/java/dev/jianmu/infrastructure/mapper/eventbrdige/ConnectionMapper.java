package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Connection;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class: ConnectionMapper
 * @description: ConnectionMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 09:12
 **/
public interface ConnectionMapper {
    @Select("SELECT * FROM `eb_connection` WHERE source_id = #{sourceId}")
    @Result(column = "source_id", property = "sourceId")
    @Result(column = "target_id", property = "targetId")
    List<Connection> findBySourceId(String sourceId);

    @Insert("insert into eb_connection(id, source_id, target_id) " +
            "values(#{id}, #{sourceId}, #{targetId})")
    void save(Connection connection);
}
