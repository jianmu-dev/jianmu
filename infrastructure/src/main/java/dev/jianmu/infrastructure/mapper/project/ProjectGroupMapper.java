package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.project.aggregate.ProjectGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Daihw
 * @class ProjectGroupMapper
 * @description 项目组关联Mapper
 * @create 2021/11/25 2:19 下午
 */
public interface ProjectGroupMapper {
    @Insert("insert into jm_project_group(id, name, description, sort, is_show, project_count, created_time, last_modified_time) " +
            "values(#{id}, #{name}, #{description}, #{sort}, #{isShow}, #{projectCount}, #{createdTime}, #{lastModifiedTime})")
    void add(ProjectGroup projectGroup);

    @Insert("<script>" +
            "INSERT INTO `jm_project_group`(`id`, `name`, `description`, `sort`, `is_show`, `project_count`, `created_time`, `last_modified_time`) VALUES" +
            " <foreach collection='projectGroups' item='i' separator=','>" +
            "   (#{i.id}, #{i.name}, #{i.description}, #{i.sort}, #{i.isShow}, #{i.projectCount}, #{i.createdTime}, #{i.lastModifiedTime})" +
            " </foreach>" +
            "</script>")
    void addAll(@Param("projectGroups") List<ProjectGroup> projectGroups);

    @Delete("delete from jm_project_group where id = #{id}")
    void deleteById(String id);

    @Select("select * from jm_project_group where id = #{id}")
    @Result(column = "is_show", property = "isShow")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<ProjectGroup> findById(String id);

    @Select("select * from jm_project_group order by sort")
    @Result(column = "is_show", property = "isShow")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<ProjectGroup> findAllOrderBySort();

    @Update("update jm_project_group set name=#{name}, description=#{description}, is_show=#{isShow}, last_modified_time=#{lastModifiedTime} where id=#{id}")
    void update(ProjectGroup projectGroup);

    @Select("select * from jm_project_group where sort between #{originSort} and #{targetSort} order by sort")
    @Result(column = "is_show", property = "isShow")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<ProjectGroup> findAllBySortBetween(@Param("originSort") Integer originSort, @Param("targetSort") Integer targetSort);

    @Select("select * from jm_project_group order by sort desc limit 1")
    @Result(column = "is_show", property = "isShow")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<ProjectGroup> findBySortMax();

    @Update("update jm_project_group set project_count = project_count + ${count} where id = #{projectGroupId}")
    void addProjectCountById(@Param("projectGroupId") String projectGroupId, @Param("count") int count);

    @Update("update jm_project_group set project_count = project_count - ${count} where id = #{projectGroupId}")
    void subProjectCountById(@Param("projectGroupId") String projectGroupId, @Param("count") int count);

    @Delete("<script>" +
            "DELETE FROM `jm_project_group` WHERE `id` IN " +
            "<foreach collection='ids' item='i'  open='(' separator=',' close=')'>#{i}" +
            "</foreach>" +
            "</script>")
    void deleteByIdIn(@Param("ids") List<String> ids);

    @Select("select * from jm_project_group where name = #{name}")
    @Result(column = "is_show", property = "isShow")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<ProjectGroup> findByName(String name);
}
