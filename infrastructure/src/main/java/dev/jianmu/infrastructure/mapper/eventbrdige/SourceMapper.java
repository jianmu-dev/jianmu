package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Source;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * @class: SourceMapper
 * @description: SourceMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 09:07
 **/
public interface SourceMapper {
    @Select("SELECT * FROM `eb_source` WHERE id = #{id}")
    @Result(column = "bridge_id", property = "bridgeId")
    Optional<Source> findById(String id);

    @Select("SELECT * FROM `eb_source` WHERE bridge_id = #{bridgeId}")
    @Result(column = "bridge_id", property = "bridgeId")
    Optional<Source> findByBridgeId(String bridgeId);

    @Update("update eb_source set token = #{token} WHERE id = #{id}")
    void updateTokenById(Source source);

    @Insert("insert into eb_source(id, bridge_id, name, type, token) " +
            "values(#{id}, #{bridgeId}, #{name}, #{type}, #{token})" +
            " ON DUPLICATE KEY UPDATE " +
            "name=#{name}, type=#{type}, token=#{token}")
    void saveOrUpdate(Source source);

    @Delete("delete FROM eb_source WHERE id = #{id}")
    void deleteById(String id);

    @Delete("delete FROM eb_source WHERE bridge_id = #{bridgeId}")
    void deleteByBridgeId(String bridgeId);
}
