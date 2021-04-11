package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.mybatis.task.WorkerParameterDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @class: WorkerParameterMapper
 * @description: 任务执行器参数Mapper
 * @author: Ethan Liu
 * @create: 2021-04-10 22:05
 **/
public interface WorkerParameterMapper {

    @Insert("<script>" +
            "insert into worker_parameter(worker_id, parameter_name, parameter_id) values" +
            "<foreach collection='parameterMap' item='value' index='key' separator=','>" +
            "(#{workerId}, #{key}, #{value})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("workerId") String workerId, @Param("parameterMap") Map<String, String> parameterMap);

    @Delete("delete from worker_parameter where worker_id = #{workerId}")
    void deleteByWorkerId(@Param("workerId") String workerId);

    @Select("select * from worker_parameter where worker_id = #{workerId}")
    List<WorkerParameterDO> findByWorkerId(@Param("workerId") String workerId);
}
