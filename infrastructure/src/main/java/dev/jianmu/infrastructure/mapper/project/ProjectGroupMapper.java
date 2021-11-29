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
    @Insert("insert into project_group(id, name, description, sort, project_count, created_time, last_modified_time) " +
            "values(#{id}, #{name}, #{description}, #{sort}, #{projectCount}, #{createdTime}, #{lastModifiedTime})")
    void add(ProjectGroup projectGroup);

    @Delete("delete from project_group where id = #{id}")
    void deleteById(String id);

    @Select("select * from project_group where id = #{id}")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<ProjectGroup> findById(String id);

    @Select("select * from project_group order by sort")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<ProjectGroup> findAllOrderBySort();

    @Update("update project_group set name=#{name}, description=#{description}, last_modified_time=#{lastModifiedTime} where id=#{id}")
    void update(ProjectGroup projectGroup);

    @Update("update project_group set sort=#{sort} where id =#{id}")
    void updateSortById(@Param("id") String id, @Param("sort") Integer sort);

    @Select("select * from project_group where sort between #{originSort} and #{targetSort}")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<ProjectGroup> findAllBySortBetween(@Param("originSort") Integer originSort, @Param("targetSort") Integer targetSort);

    @Select("select * from project_group order by sort desc limit 1")
    @Result(column = "project_count", property = "projectCount")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    Optional<ProjectGroup> findBySortMax();

    @Update("update project_group set project_count = project_count + ${count} where id = #{projectGroupId}")
    void addProjectCountById(@Param("projectGroupId") String projectGroupId, @Param("count") int count);

    @Update("update project_group set project_count = project_count - ${count} where id = #{projectGroupId}")
    void subProjectCountById(@Param("projectGroupId") String projectGroupId, @Param("count") int count);
}
