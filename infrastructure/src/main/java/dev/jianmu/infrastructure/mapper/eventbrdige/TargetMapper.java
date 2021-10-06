package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Target;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: TargetMapper
 * @description: TargetMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 09:21
 **/
public interface TargetMapper {
    @Select("SELECT * FROM `eb_target` WHERE id = #{id}")
    @Result(column = "destination_id", property = "destinationId")
    @Result(column = "bridge_id", property = "bridgeId")
    Optional<Target> findById(String id);

    @Select("SELECT * FROM `eb_target` WHERE bridge_id = #{bridgeId}")
    @Result(column = "destination_id", property = "destinationId")
    @Result(column = "bridge_id", property = "bridgeId")
    List<Target> findByBridgeId(String bridgeId);

    @Select("SELECT * FROM `eb_target` WHERE ref = #{ref}")
    @Result(column = "destination_id", property = "destinationId")
    @Result(column = "bridge_id", property = "bridgeId")
    Optional<Target> findByRef(String ref);

    @Select("SELECT EXISTS(SELECT * FROM `eb_target` WHERE id = #{id})")
    boolean checkTargetExists(@Param("id") String id);

    @Select("SELECT * FROM `eb_target` WHERE destination_id = #{destinationId}")
    @Result(column = "destination_id", property = "destinationId")
    @Result(column = "bridge_id", property = "bridgeId")
    Optional<Target> findByDestinationId(String destinationId);

    @Insert("insert into eb_target(id, ref, bridge_id, name, type, destination_id) " +
            "values(#{id}, #{ref}, #{bridgeId}, #{name}, #{type}, #{destinationId})")
    void add(Target target);

    @Update("update eb_target set ref=#{ref}, name=#{name}, type=#{type}, destination_id=#{destinationId} where id = #{id}")
    void update(Target target);

    @Delete("DELETE FROM eb_target WHERE id = #{id}")
    void deleteById(String id);

    @Delete("DELETE FROM eb_target WHERE bridge_id = #{bridgeId}")
    void deleteByBridgeId(String bridgeId);
}
