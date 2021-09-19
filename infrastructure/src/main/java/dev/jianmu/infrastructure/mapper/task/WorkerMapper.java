package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.worker.aggregate.Worker;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

/**
 * @class: WorkerMapper
 * @description: WorkerMapper
 * @author: Ethan Liu
 * @create: 2021-04-02 12:39
 **/
public interface WorkerMapper {
    @Insert("insert into worker(id,name, status, type) " +
            "values(#{id}, #{name}, #{status}, #{type})")
    void add(Worker worker);

    @Delete("delete from worker where id = #{id}")
    void delete(Worker worker);

    @Update("update worker set status = #{status} where id = #{id}")
    void updateStatus(Worker worker);

    @Select("select * from worker where id = #{workerId}")
    Optional<Worker> findById(String workerId);
}
