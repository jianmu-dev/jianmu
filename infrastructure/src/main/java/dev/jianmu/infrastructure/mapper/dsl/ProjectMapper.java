package dev.jianmu.infrastructure.mapper.dsl;

import dev.jianmu.dsl.aggregate.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: ProjectMapper
 * @description: DSL流程定义关联Mapper
 * @author: Ethan Liu
 * @create: 2021-04-23 11:39
 **/
public interface ProjectMapper {
    @Insert("insert into jianmu_project(id, dsl_url, workflow_name, workflow_ref, workflow_version, steps, dsl_text, last_modified_by, last_modified_time) " +
            "values(#{id}, #{dslUrl}, #{workflowName}, #{workflowRef}, #{workflowVersion}, #{steps}, #{dslText}, #{lastModifiedBy}, #{lastModifiedTime})")
    void add(Project project);

    @Delete("delete from jianmu_project where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Update("update jianmu_project set workflow_name = #{workflowName}, workflow_version = #{workflowVersion}, steps = #{steps}, dsl_text = #{dslText} , last_modified_by = #{lastModifiedBy}, last_modified_time = #{lastModifiedTime} " +
            "where workflow_ref = #{workflowRef}")
    void updateByWorkflowRef(Project project);

    @Select("select * from jianmu_project where id = #{id}")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "dsl_url", property = "dslUrl")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<Project> findById(String id);

    @Select("select * from jianmu_project where workflow_ref = #{workflowRef}")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "dsl_url", property = "dslUrl")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<Project> findByWorkflowRef(String workflowRef);

    @Select("<script>" +
            "SELECT * FROM `jianmu_project` " +
            "<if test='name != null'> WHERE `workflow_name` like concat('%', #{workflowName}, '%')</if>" +
            "</script>")
    @Result(column = "workflow_name", property = "workflowName")
    @Result(column = "dsl_url", property = "dslUrl")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "dsl_text", property = "dslText")
    @Result(column = "last_modified_by", property = "lastModifiedBy")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<Project> findAll(String workflowName);
}
