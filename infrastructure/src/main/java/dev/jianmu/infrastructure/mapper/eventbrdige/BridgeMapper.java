package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.Bridge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class: BridgeMapper
 * @description: BridgeMapper
 * @author: Ethan Liu
 * @create: 2021-09-27 10:52
 **/
public interface BridgeMapper {

    @Insert("insert into eb_bridge(id, name, created_time, last_modified_by, last_modified_time) " +
            "values(#{id}, #{name}, #{createdTime}, #{lastModifiedBy}, #{lastModifiedTime})" +
            " ON DUPLICATE KEY UPDATE " +
            "name=#{name}, last_modified_by=#{lastModifiedBy}, last_modified_time=#{lastModifiedTime}")
    void saveOrUpdate(Bridge bridge);

    @Delete("delete from eb_bridge where id = #{id}")
    void deleteById(String id);

    @Select("SELECT * FROM eb_bridge")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<Bridge> findAll();
}
