package dev.jianmu.infrastructure.mapper.task;

import org.apache.ibatis.annotations.*;

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
}
