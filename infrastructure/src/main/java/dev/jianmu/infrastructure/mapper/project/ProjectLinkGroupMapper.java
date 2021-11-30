package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.project.aggregate.ProjectLinkGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Daihw
 * @class ProjectLinkGroupMapper
 * @description 项目-项目组中间表关联Mapper
 * @create 2021/11/25 2:30 下午
 */
public interface ProjectLinkGroupMapper {
    @Insert("insert into project_link_group(id, project_id, project_group_id, sort, created_time) " +
            "VALUES(#{id}, #{projectId}, #{projectGroupId}, #{sort}, #{createdTime})")
    void add(ProjectLinkGroup projectLinkGroup);

    @Insert("<script>" +
            "INSERT INTO `project_link_group`(`id`, `project_id`, `project_group_id`, `sort`, `created_time`) VALUES" +
            " <foreach collection='projectLinkGroups' item='i' separator=','>" +
            "   (#{i.id}, #{i.projectId}, #{i.projectGroupId}, #{i.sort}, #{i.createdTime})" +
            " </foreach>" +
            "</script>")
    void addAll(@Param("projectLinkGroups") List<ProjectLinkGroup> projectLinkGroups);

    @Select("select project_id from project_link_group where project_group_id = #{groupId}")
    List<String> findAllProjectIdByGroupId(String groupId);

    @Select("select * from project_link_group where project_group_id = #{projectGroupId} order by sort desc limit 1")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "project_group_id", property = "projectGroupId")
    @Result(column = "created_time", property = "createdTime")
    Optional<ProjectLinkGroup> findByProjectGroupIdAndSortMax(String projectGroupId);

    @Delete("delete from project_link_group where project_group_id = #{projectGroupId}")
    void deleteByProjectGroupId(String projectGroupId);

    @Select("select * from project_link_group where id = #{projectLinkGroupId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "project_group_id", property = "projectGroupId")
    @Result(column = "created_time", property = "createdTime")
    Optional<ProjectLinkGroup> findById(String projectLinkGroupId);

    @Delete("delete from project_link_group where id = #{projectLinkGroupId}")
    void deleteById(String projectLinkGroupId);

    @Delete("<script>" +
            "DELETE FROM `project_link_group` WHERE `id` IN " +
            "<foreach collection='projectLinkGroupIds' item='i'  open='(' separator=',' close=')'>#{i}" +
            "</foreach>" +
            "</script>")
    void deleteByIdIn(@Param("projectLinkGroupIds") List<String> projectLinkGroupIds);

    @Select("select * from project_link_group where project_group_id = #{projectGroupId} and sort between #{originSort} and #{targetSort} order by sort")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "project_group_id", property = "projectGroupId")
    @Result(column = "created_time", property = "createdTime")
    List<ProjectLinkGroup> findAllByGroupIdAndSortBetween(@Param("projectGroupId") String projectGroupId,
                                                          @Param("originSort") Integer originSort,
                                                          @Param("targetSort") Integer targetSort);

    @Select("select * from project_link_group where project_id = #{projectId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "project_group_id", property = "projectGroupId")
    @Result(column = "created_time", property = "createdTime")
    Optional<ProjectLinkGroup> findByProjectId(String projectId);

    @Select("select * from project_link_group where project_group_id = #{groupId} order by sort")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "project_group_id", property = "projectGroupId")
    @Result(column = "created_time", property = "createdTime")
    List<ProjectLinkGroup> findAllByGroupId(String groupId);

    @Select("<script>" +
            "SELECT * FROM `project_link_group` where `project_id` IN " +
            "<foreach collection='projectIds' item='i'  open='(' separator=',' close=')'>#{i}" +
            "</foreach>" +
            "</script>")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "project_group_id", property = "projectGroupId")
    @Result(column = "created_time", property = "createdTime")
    List<ProjectLinkGroup> findAllByProjectIdIn(@Param("projectIds") List<String> projectIds);
}
