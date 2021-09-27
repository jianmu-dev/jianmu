package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Target;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

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
    Optional<Target> findById(String id);

    @Select("SELECT * FROM `eb_target` WHERE ref = #{ref}")
    @Result(column = "destination_id", property = "destinationId")
    Optional<Target> findByRef(String ref);

    @Select("SELECT * FROM `eb_target` WHERE destination_id = #{destinationId}")
    @Result(column = "destination_id", property = "destinationId")
    Optional<Target> findByDestinationId(String destinationId);

    @Insert("insert into eb_target(id, ref, name, type, destination_id) " +
            "values(#{id}, #{ref}, #{name}, #{type}, #{destinationId})" +
            " ON DUPLICATE KEY UPDATE " +
            "ref=#{ref}, name=#{name}, type=#{type}, destination_id=#{destinationId}")
    void saveOrUpdate(Target target);

    @Delete("DELETE FROM eb_target WHERE id = #{id}")
    void deleteById(String id);
}
