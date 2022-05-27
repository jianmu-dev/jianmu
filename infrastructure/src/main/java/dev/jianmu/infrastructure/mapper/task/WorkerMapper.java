package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.worker.aggregate.Worker;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @class WorkerMapper
 * @description WorkerMapper
 * @author Ethan Liu
 * @create 2021-04-02 12:39
*/
public interface WorkerMapper {
    @Insert("insert into worker(id, name, tags, capacity, os, arch, status, type, created_time) " +
            "values(#{id}, #{name}, #{tags}, #{capacity}, #{os}, #{arch}, #{status}, #{type}, #{createdTime})")
    void add(Worker worker);

    @Delete("delete from worker where id = #{id}")
    void delete(Worker worker);

    @Update("update worker set status = #{status} where id = #{id}")
    void updateStatus(Worker worker);

    @Select("select * from worker where id = #{workerId}")
    @Result(column = "created_time", property = "createdTime")
    Optional<Worker> findById(String workerId);

    @Select("select * from worker where type = #{type} and created_time < #{createdTime}")
    @Result(column = "created_time", property = "createdTime")
    List<Worker>  findByTypeAndCreatedTimeLessThan(@Param("type") Worker.Type type, @Param("createdTime") LocalDateTime createdTime);
}
