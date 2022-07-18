package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.project.aggregate.ProjectLastExecution;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * @class ProjectLastExecutionMapper
 * @description ProjectLastExecutionMapper
 * @author Daihw
 * @create 2022/7/18 4:09 下午
 */
@Mapper
public interface ProjectLastExecutionMapper {
    @Insert("INSERT INTO `jm_project_last_execution`(`workflow_ref`) values(#{workflowRef})")
    void add(ProjectLastExecution projectLastExecution);

    @Update("UPDATE `jm_project_last_execution` SET  `start_time` = #{startTime}, `status` = #{status}, `end_time` = #{endTime}, `suspended_time` = #{suspendedTime} " +
            "WHERE `workflow_ref` = #{workflowRef}")
    void update(ProjectLastExecution projectLastExecution);

    @Delete("DELETE FROM `jm_project_last_execution` WHERE `workflow_ref` = #{workflowRef}")
    void deleteByRef(String workflowRef);

    @Select("SELECT * FROM `jm_project_last_execution` WHERE `workflow_ref` = #{workflowRef}")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "suspended_time", property = "suspendedTime")
    Optional<ProjectLastExecution> findByRef(String workflowRef);
}
