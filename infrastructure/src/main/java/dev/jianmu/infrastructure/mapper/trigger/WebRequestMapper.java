package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.aggregate.WebRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

/**
 * @class WebRequestMapper
 * @description WebRequestMapper
 * @author Ethan Liu
 * @create 2021-11-15 13:44
 */
public interface WebRequestMapper {
    @Insert("insert into jianmu_web_request(id, project_id, workflow_ref, workflow_version, trigger_id, user_agent, status_code, error_msg, request_time) " +
            "values(#{id}, #{projectId}, #{workflowRef}, #{workflowVersion}, #{triggerId}, #{userAgent}, #{statusCode}, #{errorMsg}, #{requestTime})")
    void add(WebRequest webRequest);

    @Update("UPDATE jianmu_web_request set status_code = #{statusCode}, error_msg = #{errorMsg} where id = #{id}")
    void update(WebRequest webRequest);

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

    @Select("SELECT * FROM jianmu_web_request where id = #{id}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "user_agent", property = "userAgent")
    @Result(column = "status_code", property = "statusCode")
    @Result(column = "error_msg", property = "errorMsg")
    @Result(column = "request_time", property = "requestTime")
    Optional<WebRequest> findById(String id);

    @Select("select t2.* from jianmu_trigger_event t1 " +
            "join jianmu_web_request t2 on t1.web_request_id = (t2.id collate utf8mb4_0900_ai_ci) " +
            "where t1.id = #{triggerId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "user_agent", property = "userAgent")
    @Result(column = "status_code", property = "statusCode")
    @Result(column = "error_msg", property = "errorMsg")
    @Result(column = "request_time", property = "requestTime")
    Optional<WebRequest> findByTriggerId(String triggerId);

    @Select("SELECT * FROM jianmu_web_request where project_id = #{projectId} order by request_time desc limit 1")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "user_agent", property = "userAgent")
    @Result(column = "status_code", property = "statusCode")
    @Result(column = "error_msg", property = "errorMsg")
    @Result(column = "request_time", property = "requestTime")
    Optional<WebRequest> findLatestByProjectId(String projectId);

    @Select("select * from jianmu_web_request where project_id = #{projectId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "user_agent", property = "userAgent")
    @Result(column = "status_code", property = "statusCode")
    @Result(column = "error_msg", property = "errorMsg")
    @Result(column = "request_time", property = "requestTime")
    List<WebRequest> findByProjectId(String projectId);
}
