package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.aggregate.WebRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class WebRequestMapper
 * @description WebRequestMapper
 * @author Ethan Liu
 * @create 2021-11-15 13:44
 */
public interface WebRequestMapper {
    @Insert("insert into jianmu_web_request(id, project_id, workflow_ref, workflow_version, trigger_id, user_agent, payload, status_code, error_msg, request_time) " +
            "values(#{id}, #{projectId}, #{workflowRef}, #{workflowVersion}, #{triggerId}, #{userAgent}, #{payload}, #{statusCode}, #{errorMsg}, #{requestTime})")
    void add(WebRequest webRequest);

    @Select("SELECT * FROM jianmu_web_request where project_id = #{projectId} order by request_time desc")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "user_agent", property = "userAgent")
    @Result(column = "status_code", property = "statusCode")
    @Result(column = "error_msg", property = "errorMsg")
    @Result(column = "request_time", property = "requestTime")
    List<WebRequest> findPage(String projectId);
}
