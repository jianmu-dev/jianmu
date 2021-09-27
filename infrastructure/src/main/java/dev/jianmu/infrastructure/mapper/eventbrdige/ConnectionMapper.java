package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Connection;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

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
    @Result(column = "bridge_id", property = "bridgeId")
    List<Connection> findBySourceId(String sourceId);

    @Select("SELECT * FROM `eb_connection` WHERE target_id = #{targetId}")
    @Result(column = "source_id", property = "sourceId")
    @Result(column = "target_id", property = "targetId")
    @Result(column = "bridge_id", property = "bridgeId")
    List<Connection> findByTargetId(String targetId);

    @Insert("insert into eb_connection(id, source_id, target_id) " +
            "values(#{id}, #{sourceId}, #{targetId})")
    void save(Connection connection);

    @Insert("<script>" +
            "insert into eb_connection(id, bridge_id, source_id, target_id)  values" +
            "<foreach collection='connections' item='i' index='index' separator=','>" +
            "(#{i.id}, #{i.bridgeId}, #{i.sourceId}, #{i.targetId})" +
            "</foreach>" +
            " ON DUPLICATE KEY UPDATE " +
            "source_id=VALUES(source_id), target_id=VALUES(target_id)" +
            " </script>")
    void saveOrUpdateList(@Param("connections") Set<Connection> connections);

    @Delete("delete from eb_connection where id = #{id}")
    void deleteById(String id);
}
