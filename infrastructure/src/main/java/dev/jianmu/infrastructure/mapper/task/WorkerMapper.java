package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.worker.aggregate.Worker;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class WorkerMapper
 * @description WorkerMapper
 * @create 2021-04-02 12:39
 */
public interface WorkerMapper {
    @Insert("insert into jm_worker(id, name, tags, capacity, os, arch, status, type, created_time) " +
            "values(#{id}, #{name}, #{tags}, #{capacity}, #{os}, #{arch}, #{status}, #{type}, #{createdTime})")
    void add(Worker worker);

    @Delete("delete from jm_worker where id = #{id}")
    void delete(Worker worker);

    @Update("update jm_worker set status = #{status} where id = #{id}")
    void updateStatus(Worker worker);

    @Update("update worker set tags = #{tags} where id = #{id}")
    void updateTag(Worker worker);

    @Select("select * from jm_worker where id = #{workerId}")
    @Result(column = "created_time", property = "createdTime")
    Optional<Worker> findById(String workerId);

    @Select("<script> " +
            "select * from jm_worker" +
            "<where>" +
            " type IN " +
            " <foreach collection='types' item='item' open='(' close=')' separator=','> #{item} " +
            " </foreach>" +
            "</where>" +
            "</script>")
    @Result(column = "created_time", property = "createdTime")
    List<Worker> findByTypeInAndCreatedTimeLessThan(@Param("types") List<Worker.Type> types, @Param("createdTime") LocalDateTime createdTime);

    @Select("<script> " +
            "select * from worker" +
            "<where>" +
            " type IN " +
            " <foreach collection='types' item='item' open='(' close=')' separator=','> #{item} " +
            " </foreach>" +
            " and " +
            " tags IN " +
            " <foreach collection='tags' item='tag' open='(' close=')' separator=','> #{tag} " +
            " </foreach>" +
            "</where>" +
            "</script>")
    @Result(column = "created_time", property = "createdTime")
    List<Worker> findByTypeInAndTagAndCreatedTimeLessThan(@Param("types") List<Worker.Type> types, @Param("tags") List<String> tags, @Param("createdTime") LocalDateTime createdTime);

}
