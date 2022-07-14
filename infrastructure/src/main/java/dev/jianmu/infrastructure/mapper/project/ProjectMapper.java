package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.query.ProjectVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class ProjectMapper
 * @description DSL流程定义关联Mapper
 * @create 2021-04-23 11:39
 */
public interface ProjectMapper {
    @Insert("insert into jm_project(id, dsl_source, dsl_type, enabled, mutable, trigger_type, git_repo_id, workflow_name, workflow_description, workflow_ref, workflow_version, steps, dsl_text, created_time, last_modified_by, last_modified_time, concurrent, association_id, association_type) " +
            "values(#{id}, #{dslSource}, #{dslType}, #{enabled}, #{mutable}, #{triggerType}, #{gitRepoId}, #{workflowName}, #{workflowDescription}, #{workflowRef}, #{workflowVersion}, #{steps}, #{dslText}, #{createdTime}, #{lastModifiedBy}, #{lastModifiedTime}, #{concurrent}, #{associationId}, #{associationType})")
    void add(Project project);

    @Delete("delete from jm_project where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Update("update jm_project set dsl_type = #{dslType}, enabled = #{enabled}, mutable = #{mutable}, concurrent = #{concurrent}, trigger_type = #{triggerType}, workflow_name = #{workflowName}, workflow_description = #{workflowDescription}, workflow_version = #{workflowVersion}, steps = #{steps}, dsl_text = #{dslText} , last_modified_by = #{lastModifiedBy}, last_modified_time = #{lastModifiedTime} " +
            "where workflow_ref = #{workflowRef}")
    void updateByWorkflowRef(Project project);

    @Select("select * from jm_project where id = #{id}")
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
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    Optional<Project> findById(String id);

    @Select("select * from jm_project where workflow_name = #{name}")
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
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    Optional<Project> findByName(String name);

    @Select("select * from jm_project where workflow_ref = #{workflowRef}")
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
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    Optional<Project> findByWorkflowRef(String workflowRef);

    @Select("<script>" +
            "SELECT * FROM `jm_project` " +
            "<if test='name != null'> WHERE `workflow_name` like concat('%', #{workflowName}, '%')</if>" +
            " order by last_modified_time desc" +
            "</script>")
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
    List<Project> findAllPage(String workflowName);

    @Select("select * from jm_project order by created_time desc")
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
    List<Project> findAll();

    @Select("<script>" +
            "SELECT jp.*, `wi`.`end_time`, `wi`.`status`, `wi`.`start_time`, `wi`.`suspended_time` FROM `jm_project` `jp` INNER JOIN `jm_project_link_group` `plp`  ON `plp`.`project_id` = `jp`.`id` " +
            "LEFT JOIN (select  `t1`.`workflow_ref`, `t1`.`end_time`, `t1`.`start_time`, `t1`.`suspended_time`, `t1`.`status`,  if(`end_time` is null, now(), `end_time`) `sort_end_time` " +
            "           FROM `jm_workflow_instance` `t1`, (select `workflow_ref`,max(`serial_no`) `serial_no` from `jm_workflow_instance` GROUP BY `workflow_ref`) t2 " +
            "           WHERE `t1`.`workflow_ref` = `t2`.`workflow_ref` and `t1`.`serial_no` = `t2`.`serial_no`) `wi` " +
            "ON `wi`.`workflow_ref` = `jp`.`workflow_ref` COLLATE utf8mb4_unicode_ci " +
            "<where>" +
            "   <if test='associationId != null'> AND `jp`.`association_id` = #{associationId} </if>" +
            "   <if test='associationType != null'> AND `jp`.`association_type` = #{associationType} </if>" +
            "   <if test='projectGroupId != null'> AND `plp`.`project_group_id` = #{projectGroupId} </if>" +
            "   <if test='workflowName != null'> AND (`jp`.`workflow_name` like concat('%', #{workflowName}, '%') OR `jp`.`workflow_description` like concat('%', #{workflowName}, '%'))</if>" +
            "</where>" +
            "<if test='sortType == \"DEFAULT_SORT\"'> ORDER BY `plp`.`sort` asc</if>" +
            "<if test='sortType == \"LAST_MODIFIED_TIME\"'> ORDER BY `jp`.`last_modified_time` desc</if>" +
            "<if test='sortType == \"LAST_EXECUTION_TIME\"'> ORDER BY `wi`.`sort_end_time` desc</if>" +
            "</script>")
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
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "suspended_time", property = "suspendedTime")
    @Result(column = "end_time", property = "latestTime")
    List<ProjectVo> findAllByGroupId(@Param("projectGroupId") String projectGroupId,
                                     @Param("workflowName") String workflowName,
                                     @Param("sortType") String sortType,
                                     @Param("associationId") String associationId,
                                     @Param("associationType") String associationType);

    @Select("<script>" +
            "SELECT jp.*, `wi`.`end_time`, `wi`.`status`, `wi`.`start_time`, `wi`.`suspended_time` FROM `jm_project` `jp` " +
            "LEFT JOIN (select  `t1`.`workflow_ref`, `t1`.`end_time`, `t1`.`start_time`, `t1`.`suspended_time`, `t1`.`status`,  if(`end_time` is null, now(), `end_time`) `sort_end_time` " +
            "           FROM `jm_workflow_instance` `t1`, (select `workflow_ref`,max(`serial_no`) `serial_no` from `jm_workflow_instance` GROUP BY `workflow_ref`) t2 " +
            "           WHERE `t1`.`workflow_ref` = `t2`.`workflow_ref` and `t1`.`serial_no` = `t2`.`serial_no`) `wi` " +
            "ON `wi`.`workflow_ref` = `jp`.`workflow_ref` COLLATE utf8mb4_unicode_ci " +
            "<where>" +
            "   `jp`.`id` IN <foreach collection='ids' item='item' open='(' separator=',' close=')'> #{item}" +
            "   </foreach>" +
            "   <if test='status != null and status != \"INIT\"'> AND `wi`.`status` = #{status} </if>" +
            "   <if test='status == \"INIT\"'> AND `wi`.`workflow_ref` is null </if>" +
            "   <if test='workflowName != null'> AND (`jp`.`workflow_name` like concat('%', #{workflowName}, '%') OR `jp`.`workflow_description` like concat('%', #{workflowName}, '%'))</if>" +
            "</where>" +
            "<if test='sortType == \"LAST_MODIFIED_TIME\"'> ORDER BY `jp`.`last_modified_time` desc</if>" +
            "<if test='sortType == \"LAST_EXECUTION_TIME\"'> ORDER BY `wi`.`sort_end_time` desc</if>" +
            "</script>")
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
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "suspended_time", property = "suspendedTime")
    @Result(column = "end_time", property = "latestTime")
    @Result(column = "association_id", property = "associationId")
    @Result(column = "association_type", property = "associationType")
    List<ProjectVo> findByIdIn(@Param("ids") List<String> ids, @Param("workflowName") String workflowName, @Param("sortType") String sortType, @Param("status") String status);
}
