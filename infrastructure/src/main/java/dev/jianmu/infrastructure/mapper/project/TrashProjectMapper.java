package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.project.aggregate.Project;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class TrashProjectMapper
 * @description TrashProjectMapper
 * @author Daihw
 * @create 2023/5/22 10:34 上午
 */
public interface TrashProjectMapper {
    @Insert("insert into jianmu_trash_project(id, dsl_source, dsl_type, enabled, mutable, trigger_type, git_repo_id, workflow_name, workflow_description, workflow_ref, workflow_version, steps, dsl_text, created_time, last_modified_by, last_modified_time, concurrent) " +
        "values(#{id}, #{dslSource}, #{dslType}, #{enabled}, #{mutable}, #{triggerType}, #{gitRepoId}, #{workflowName}, #{workflowDescription}, #{workflowRef}, #{workflowVersion}, #{steps}, #{dslText}, #{createdTime}, #{lastModifiedBy}, #{lastModifiedTime}, #{concurrent})")
    void add(Project project);

    @Delete("delete from jianmu_trash_project where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Select("select * from jianmu_trash_project where id = #{id}")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "workflow_description", property = "workflowDescription")
    @Result(column = "dsl_source", property = "dslSource")
    @Result(column = "dsl_type", property = "dslType")
    @Result(column = "event_bridge_id", property = "eventBridgeId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "git_repo_id", property = "gitRepoId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<Project> findById(String id);
}
